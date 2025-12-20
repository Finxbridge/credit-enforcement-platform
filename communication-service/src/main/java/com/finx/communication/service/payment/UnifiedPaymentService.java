package com.finx.communication.service.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.communication.domain.dto.payment.*;
import com.finx.communication.domain.entity.PaymentGatewayTransaction;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.PaymentGatewayTransactionRepository;
import com.finx.communication.service.IntegrationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified Payment Service - handles all payment types dynamically
 * 4 APIs: initiate, status, cancel, refund
 * Service types: DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedPaymentService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final PaymentGatewayTransactionRepository paymentRepository;
    private final ObjectMapper objectMapper;

    /**
     * Initiate payment based on serviceType
     */
    public UnifiedPaymentResponse initiate(UnifiedPaymentRequest request) {
        String serviceType = request.getServiceType().toUpperCase();
        log.info("Initiating {} payment for amount: {}", serviceType, request.getAmount());

        validateInitRequest(request, serviceType);

        ThirdPartyIntegrationMaster config = getConfig(serviceType);

        // Validate all required endpoints are configured
        validateApiEndpoint(config);
        String endpoint = getInitEndpoint(config, serviceType);

        Map<String, Object> requestBody = buildInitRequestBody(request, serviceType, config);

        // Special handling for DQR (returns image)
        if ("DYNAMIC_QR".equals(serviceType)) {
            return initiateDqr(request, config, requestBody);
        }

        String url = config.getApiEndpoint() + endpoint;
        log.debug("Calling init API: {}", url);
        Map<String, Object> response = callApi(url, requestBody, config);

        return saveAndBuildResponse(request, serviceType, response, config);
    }

    /**
     * Check payment status
     * FinxBridge API: POST /api/v1/service/status
     * Request: { merchantId, provider, transactionId (TX...), serviceType }
     * Response: { success, code, message, data: { merchantId, transactionId, amount, paymentState, paymentInstrument } }
     */
    public UnifiedPaymentResponse status(UnifiedStatusRequest request) {
        String serviceType = request.getServiceType().toUpperCase();
        log.info("Checking {} status for transaction: {}", serviceType, request.getTransactionId());

        PaymentGatewayTransaction transaction = findTransaction(request.getTransactionId());
        ThirdPartyIntegrationMaster config = getConfig(serviceType);

        // Validate and get status endpoint from config
        validateApiEndpoint(config);
        String statusEndpoint = getStatusEndpoint(config, serviceType);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        // Use gatewayOrderId which contains the gateway's transactionId (TX...) from init response
        // The internal transactionId is our UUID used for lookup, not what the gateway expects
        requestBody.put("transactionId", transaction.getGatewayOrderId());
        requestBody.put("serviceType", serviceType);
        log.debug("Status check - sending gateway transactionId: {}", transaction.getGatewayOrderId());

        String url = config.getApiEndpoint() + statusEndpoint;
        log.debug("Calling status API: {}", url);
        Map<String, Object> response = callApi(url, requestBody, config);

        // API response structure: { success, code, message, data: { paymentState, ... } }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            data = response; // Fallback for flat response
        }

        // Extract status from response.data.paymentState
        String paymentState = extractString(data, "paymentState", "status");
        String newStatus = mapPaymentState(paymentState);
        transaction.setStatus(newStatus);
        transaction.setGatewayResponse(response);

        if ("SUCCESS".equals(newStatus) || "COMPLETED".equals(newStatus)) {
            transaction.setPaymentReceivedAt(LocalDateTime.now());
            transaction.setGatewayPaymentId(extractString(data, "paymentId", "payment_id"));
        } else if ("FAILED".equals(newStatus)) {
            transaction.setFailureReason(extractString(data, "failureReason", "error", "message"));
        }

        paymentRepository.save(transaction);
        return buildResponseFromTransaction(transaction, serviceType, response);
    }

    private String mapPaymentState(String paymentState) {
        if (paymentState == null) return "UNKNOWN";
        return switch (paymentState.toUpperCase()) {
            case "PENDING" -> "PENDING";
            case "SUCCESS", "COMPLETED", "PAID" -> "SUCCESS";
            case "FAILED", "FAILURE" -> "FAILED";
            case "CANCELLED", "CANCELED" -> "CANCELLED";
            case "EXPIRED" -> "EXPIRED";
            case "REFUNDED" -> "REFUNDED";
            default -> paymentState.toUpperCase();
        };
    }

    /**
     * Cancel payment
     * FinxBridge API: POST /api/v1/service/cancel
     * Request: { merchantId, transactionId (TX...), reason, provider, serviceType }
     */
    public UnifiedPaymentResponse cancel(UnifiedCancelRequest request) {
        String serviceType = request.getServiceType().toUpperCase();
        log.info("Cancelling {} transaction: {}", serviceType, request.getTransactionId());

        PaymentGatewayTransaction transaction = findTransaction(request.getTransactionId());

        // Validate
        if (isCompletedStatus(transaction.getStatus())) {
            throw new ApiCallException("Cannot cancel transaction with status: " + transaction.getStatus());
        }

        ThirdPartyIntegrationMaster config = getConfig(serviceType);

        // Validate and get cancel endpoint from config
        validateApiEndpoint(config);
        String cancelEndpoint = getCancelEndpoint(config, serviceType);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        // Use gatewayOrderId which contains the gateway's transactionId (TX...) from init response
        requestBody.put("transactionId", transaction.getGatewayOrderId());
        requestBody.put("reason", request.getReason() != null ? request.getReason() : "Cancelled by user");
        requestBody.put("serviceType", serviceType);
        log.debug("Cancel - sending gateway transactionId: {}", transaction.getGatewayOrderId());

        String url = config.getApiEndpoint() + cancelEndpoint;
        log.debug("Calling cancel API: {}", url);
        Map<String, Object> response = callApi(url, requestBody, config);

        transaction.setStatus("CANCELLED");
        transaction.setFailureReason(request.getReason());
        transaction.setGatewayResponse(response);
        paymentRepository.save(transaction);

        return buildResponseFromTransaction(transaction, serviceType, response);
    }

    /**
     * Refund payment
     * FinxBridge API: POST /api/v1/service/refund
     * Request: { merchantId, originalTransactionId (TX...), amount, merchantOrderId (MO...), message, provider, serviceType }
     */
    public UnifiedPaymentResponse refund(UnifiedRefundRequest request) {
        String serviceType = request.getServiceType().toUpperCase();
        log.info("Refunding {} transaction: {}", serviceType, request.getTransactionId());

        PaymentGatewayTransaction transaction = findTransaction(request.getTransactionId());

        // Validate
        if (!"SUCCESS".equalsIgnoreCase(transaction.getStatus()) &&
            !"COMPLETED".equalsIgnoreCase(transaction.getStatus())) {
            throw new ApiCallException("Cannot refund transaction with status: " + transaction.getStatus());
        }

        ThirdPartyIntegrationMaster config = getConfig(serviceType);

        // Validate and get refund endpoint from config
        validateApiEndpoint(config);
        String refundEndpoint = getRefundEndpoint(config, serviceType);

        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : transaction.getAmount();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        // Use gatewayOrderId which contains the gateway's transactionId (TX...) from init response
        requestBody.put("originalTransactionId", transaction.getGatewayOrderId());
        requestBody.put("amount", refundAmount.intValue()); // FinxBridge expects integer amount in paise
        requestBody.put("merchantOrderId", transaction.getGatewayOrderId());
        requestBody.put("message", request.getReason() != null ? request.getReason() : "Refund requested");
        requestBody.put("serviceType", serviceType);
        log.debug("Refund - sending gateway transactionId: {}", transaction.getGatewayOrderId());

        String url = config.getApiEndpoint() + refundEndpoint;
        log.debug("Calling refund API: {}", url);
        Map<String, Object> response = callApi(url, requestBody, config);

        // API response structure: { success, code, message, data: { status, ... } }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            data = response; // Fallback for flat response
        }

        String refundStatus = extractString(data, "status", "UNKNOWN").toUpperCase();
        if ("SUCCESS".equals(refundStatus) || "REFUNDED".equals(refundStatus)) {
            transaction.setStatus("REFUNDED");
            transaction.setRefundAmount(refundAmount);
            transaction.setRefundedAt(LocalDateTime.now());
        } else {
            transaction.setStatus("REFUND_" + refundStatus);
        }
        transaction.setGatewayResponse(response);
        paymentRepository.save(transaction);

        return buildResponseFromTransaction(transaction, serviceType, response);
    }

    /**
     * Get transaction details
     */
    public UnifiedPaymentResponse getTransaction(String transactionId) {
        PaymentGatewayTransaction transaction = findTransaction(transactionId);
        return buildResponseFromTransaction(transaction, null, null);
    }

    /**
     * Get transactions by case
     */
    public List<UnifiedPaymentResponse> getTransactionsByCase(Long caseId) {
        return paymentRepository.findByCaseId(caseId).stream()
                .map(t -> buildResponseFromTransaction(t, null, null))
                .collect(Collectors.toList());
    }

    // ==================== Private Methods ====================

    private void validateInitRequest(UnifiedPaymentRequest request, String serviceType) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiCallException("Amount must be positive");
        }

        switch (serviceType) {
            case "PAYMENT_LINK" -> {
                if (request.getMobileNumber() == null || request.getMobileNumber().isBlank()) {
                    throw new ApiCallException("Mobile number is required for PAYMENT_LINK");
                }
            }
            case "COLLECT_CALL" -> {
                // instrumentType comes from database config (always MOBILE)
                // instrumentReference is the mobile number - required from request
                if (request.getInstrumentReference() == null || request.getInstrumentReference().isBlank()) {
                    throw new ApiCallException("Mobile number (instrumentReference) is required for COLLECT_CALL");
                }
            }
        }
    }

    private ThirdPartyIntegrationMaster getConfig(String serviceType) {
        String integrationName = switch (serviceType) {
            case "DYNAMIC_QR" -> "FINXBRIDGE_DQR";
            case "COLLECT_CALL" -> "FINXBRIDGE_COLLECT_CALL";
            default -> "FINXBRIDGE_PAYMENT_LINK";
        };
        return integrationCacheService.getIntegration(integrationName)
                .orElseThrow(() -> new ConfigurationNotFoundException("Config not found: " + integrationName));
    }

    /**
     * Get init endpoint from database configuration.
     * Throws ConfigurationNotFoundException if not configured.
     */
    private String getInitEndpoint(ThirdPartyIntegrationMaster config, String serviceType) {
        String endpoint = config.getConfigValueAsString("init_endpoint");
        if (endpoint == null || endpoint.isBlank()) {
            throw new ConfigurationNotFoundException(
                "init_endpoint not configured for " + serviceType +
                ". Please add 'init_endpoint' to config_json in third_party_integration_master for integration: " +
                config.getIntegrationName());
        }
        return endpoint;
    }

    /**
     * Get status endpoint from database configuration.
     * Throws ConfigurationNotFoundException if not configured.
     */
    private String getStatusEndpoint(ThirdPartyIntegrationMaster config, String serviceType) {
        String endpoint = config.getConfigValueAsString("status_endpoint");
        if (endpoint == null || endpoint.isBlank()) {
            throw new ConfigurationNotFoundException(
                "status_endpoint not configured for " + serviceType +
                ". Please add 'status_endpoint' to config_json in third_party_integration_master for integration: " +
                config.getIntegrationName());
        }
        return endpoint;
    }

    /**
     * Get cancel endpoint from database configuration.
     * Throws ConfigurationNotFoundException if not configured.
     */
    private String getCancelEndpoint(ThirdPartyIntegrationMaster config, String serviceType) {
        String endpoint = config.getConfigValueAsString("cancel_endpoint");
        if (endpoint == null || endpoint.isBlank()) {
            throw new ConfigurationNotFoundException(
                "cancel_endpoint not configured for " + serviceType +
                ". Please add 'cancel_endpoint' to config_json in third_party_integration_master for integration: " +
                config.getIntegrationName());
        }
        return endpoint;
    }

    /**
     * Get refund endpoint from database configuration.
     * Throws ConfigurationNotFoundException if not configured.
     */
    private String getRefundEndpoint(ThirdPartyIntegrationMaster config, String serviceType) {
        String endpoint = config.getConfigValueAsString("refund_endpoint");
        if (endpoint == null || endpoint.isBlank()) {
            throw new ConfigurationNotFoundException(
                "refund_endpoint not configured for " + serviceType +
                ". Please add 'refund_endpoint' to config_json in third_party_integration_master for integration: " +
                config.getIntegrationName());
        }
        return endpoint;
    }

    /**
     * Validate base API endpoint is configured.
     * Throws ConfigurationNotFoundException if not configured.
     */
    private void validateApiEndpoint(ThirdPartyIntegrationMaster config) {
        if (config.getApiEndpoint() == null || config.getApiEndpoint().isBlank()) {
            throw new ConfigurationNotFoundException(
                "api_endpoint not configured for integration: " + config.getIntegrationName() +
                ". Please set the api_endpoint column in third_party_integration_master.");
        }
    }

    private Map<String, Object> buildInitRequestBody(UnifiedPaymentRequest request, String serviceType,
                                                      ThirdPartyIntegrationMaster config) {
        Map<String, Object> body = new HashMap<>();
        body.put("merchantId", config.getConfigValueAsString("merchant_id"));
        body.put("provider", config.getConfigValueAsString("provider"));
        body.put("storeId", config.getConfigValueAsString("store_id"));
        body.put("amount", request.getAmount().intValue());

        switch (serviceType) {
            case "PAYMENT_LINK" -> {
                body.put("mobileNumber", request.getMobileNumber());
                body.put("terminalId", config.getConfigValueAsString("terminal_id"));
                // Message is REQUIRED for Payment Link API (per API_DOCUMENTATION.md)
                // Use request message if provided, otherwise use default_message from config
                String message = request.getMessage();
                if (message == null || message.isBlank()) {
                    message = config.getConfigValueAsString("default_message");
                }
                if (message == null || message.isBlank()) {
                    throw new ApiCallException(
                        "Message is required for PAYMENT_LINK. Please provide message in request or configure " +
                        "'default_message' in config_json for integration: " + config.getIntegrationName());
                }
                body.put("message", message);
            }
            case "COLLECT_CALL" -> {
                // instrumentType is always MOBILE (configured in database)
                String instrumentType = config.getConfigValueAsString("instrument_type");
                if (instrumentType == null || instrumentType.isBlank()) {
                    instrumentType = "MOBILE"; // Fallback for backward compatibility
                }
                body.put("instrumentType", instrumentType);
                // instrumentReference is the mobile number from the request
                body.put("instrumentReference", request.getInstrumentReference());
            }
        }
        return body;
    }

    private UnifiedPaymentResponse initiateDqr(UnifiedPaymentRequest request, ThirdPartyIntegrationMaster config,
                                                Map<String, Object> requestBody) {
        String initEndpoint = getInitEndpoint(config, "DYNAMIC_QR");
        String url = config.getApiEndpoint() + initEndpoint;
        log.debug("Calling DQR init API: {}", url);
        try {
            // Use exchangeToMono to capture both body and headers
            var responseEntity = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> addHeaders(h, config))
                    .bodyValue(requestBody)
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();

            byte[] qrImageBytes = responseEntity != null ? responseEntity.getBody() : null;

            // Extract gateway transaction ID and merchant order ID from response headers
            // FinxBridge returns: transactionId (e.g., TX83455254424851) and merchantOrderId (e.g., MO83455255267768)
            String gatewayTransactionId = null;
            String merchantOrderId = null;
            if (responseEntity != null && responseEntity.getHeaders() != null) {
                gatewayTransactionId = responseEntity.getHeaders().getFirst("transactionId");
                merchantOrderId = responseEntity.getHeaders().getFirst("merchantOrderId");
                log.info("DQR Response Headers - transactionId: {}, merchantOrderId: {}",
                        gatewayTransactionId, merchantOrderId);
            }

            // Use gateway's transactionId as our primary transaction identifier
            String transactionId = gatewayTransactionId != null ? gatewayTransactionId : UUID.randomUUID().toString();
            String qrBase64 = qrImageBytes != null ? Base64.getEncoder().encodeToString(qrImageBytes) : null;

            PaymentGatewayTransaction transaction = PaymentGatewayTransaction.builder()
                    .transactionId(transactionId)
                    .gatewayOrderId(merchantOrderId) // Store merchant order ID
                    .gatewayName("FINXBRIDGE")
                    .caseId(request.getCaseId())
                    .loanAccountNumber(request.getLoanAccountNumber())
                    .amount(request.getAmount())
                    .currency("INR")
                    .status("CREATED")
                    .customerMobile(request.getMobileNumber())
                    .build();
            paymentRepository.save(transaction);

            return UnifiedPaymentResponse.builder()
                    .serviceType("DYNAMIC_QR")
                    .transactionId(transactionId) // Gateway's transactionId (TX...)
                    .gatewayOrderId(merchantOrderId) // Gateway's merchantOrderId (MO...)
                    .caseId(transaction.getCaseId())
                    .loanAccountNumber(transaction.getLoanAccountNumber())
                    .amount(request.getAmount())
                    .currency("INR")
                    .status("CREATED")
                    .message("Dynamic QR generated successfully")
                    .qrCodeBase64(qrBase64)
                    .createdAt(transaction.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate Dynamic QR", e);
            throw new ApiCallException("Failed to generate Dynamic QR: " + e.getMessage());
        }
    }

    private UnifiedPaymentResponse saveAndBuildResponse(UnifiedPaymentRequest request, String serviceType,
                                                         Map<String, Object> response, ThirdPartyIntegrationMaster config) {
        String transactionId = UUID.randomUUID().toString();

        // API response structure: { success, code, message, data: { transactionId, payLink, upiIntent, ... } }
        // Extract the 'data' object from response
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            // Fallback: maybe the response is flat (no data wrapper)
            data = response;
            log.warn("Response has no 'data' field, using response directly");
        }

        String gatewayOrderId = extractString(data, "transactionId", "orderId", "order_id");
        // FinxBridge returns payLink and upiIntent for Payment Link
        String paymentLink = extractString(data, "payLink", "paymentLink", "payment_link", "url");
        String upiIntent = extractString(data, "upiIntent", "upi_intent");
        String qrCodeUrl = extractString(data, "qrCodeUrl", "qr_code_url");
        String merchantOrderId = extractString(data, "merchantOrderId", "merchant_order_id");
        String providerReferenceId = extractString(data, "providerReferenceId", "provider_reference_id");

        log.debug("Extracted from response.data - payLink: {}, upiIntent: {}, transactionId: {}, merchantOrderId: {}",
                paymentLink, upiIntent, gatewayOrderId, merchantOrderId);

        PaymentGatewayTransaction transaction = PaymentGatewayTransaction.builder()
                .transactionId(transactionId)
                .gatewayOrderId(gatewayOrderId != null ? gatewayOrderId : UUID.randomUUID().toString())
                .gatewayName("FINXBRIDGE")
                .caseId(request.getCaseId())
                .loanAccountNumber(request.getLoanAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .status("CREATED")
                .paymentLink(paymentLink)
                .customerMobile(request.getMobileNumber())
                .customerEmail(request.getCustomerEmail())
                .gatewayResponse(response)
                .build();
        paymentRepository.save(transaction);

        return UnifiedPaymentResponse.builder()
                .serviceType(serviceType)
                .transactionId(transactionId)
                .gatewayOrderId(merchantOrderId != null ? merchantOrderId : transaction.getGatewayOrderId())
                .providerReferenceId(providerReferenceId)
                .caseId(transaction.getCaseId())
                .loanAccountNumber(transaction.getLoanAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .status("CREATED")
                .message(serviceType + " initiated successfully")
                .paymentLink(paymentLink)
                .upiIntent(upiIntent)
                .qrCodeUrl(qrCodeUrl)
                .createdAt(transaction.getCreatedAt())
                .gatewayResponse(response)
                .build();
    }

    private UnifiedPaymentResponse buildResponseFromTransaction(PaymentGatewayTransaction t, String serviceType,
                                                                 Map<String, Object> response) {
        return UnifiedPaymentResponse.builder()
                .serviceType(serviceType)
                .transactionId(t.getTransactionId())
                .gatewayOrderId(t.getGatewayOrderId())
                .gatewayTransactionId(t.getGatewayPaymentId())
                .caseId(t.getCaseId())
                .loanAccountNumber(t.getLoanAccountNumber())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .message("Transaction retrieved")
                .paymentLink(t.getPaymentLink())
                .refundAmount(t.getRefundAmount())
                .failureReason(t.getFailureReason())
                .createdAt(t.getCreatedAt())
                .paidAt(t.getPaymentReceivedAt())
                .refundedAt(t.getRefundedAt())
                .gatewayResponse(response != null ? response : t.getGatewayResponse())
                .build();
    }

    private PaymentGatewayTransaction findTransaction(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ApiCallException("Transaction not found: " + transactionId));
    }

    private boolean isCompletedStatus(String status) {
        return "SUCCESS".equalsIgnoreCase(status) ||
               "COMPLETED".equalsIgnoreCase(status) ||
               "REFUNDED".equalsIgnoreCase(status);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callApi(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            log.debug("Calling API: {} with body: {}", url, body);
            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> addHeaders(h, config))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(new ApiCallException("API call failed: " + e.getMessage())))
                    .block();
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (ApiCallException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiCallException("Failed to call API: " + e.getMessage());
        }
    }

    private void addHeaders(org.springframework.http.HttpHeaders headers, ThirdPartyIntegrationMaster config) {
        String apiKey = config.getConfigValueAsString("api_key");
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-API-Key", apiKey);
        }
        String authToken = config.getConfigValueAsString("auth_token");
        if (authToken != null && !authToken.isEmpty()) {
            headers.setBearerAuth(authToken);
        }
    }

    private String extractString(Map<String, Object> response, String... keys) {
        if (response == null) return null;

        // Check in data object first
        Object data = response.get("data");
        if (data instanceof Map) {
            for (String key : keys) {
                Object val = ((Map<?, ?>) data).get(key);
                if (val != null) return val.toString();
            }
        }

        // Check root
        for (String key : keys) {
            Object val = response.get(key);
            if (val != null) return val.toString();
        }
        return null;
    }
}

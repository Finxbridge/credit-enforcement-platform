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
        Map<String, Object> requestBody = buildInitRequestBody(request, serviceType, config);
        String endpoint = getInitEndpoint(serviceType);

        // Special handling for DQR (returns image)
        if ("DYNAMIC_QR".equals(serviceType)) {
            return initiateDqr(request, config, requestBody);
        }

        String url = config.getApiEndpoint() + endpoint;
        Map<String, Object> response = callApi(url, requestBody, config);

        return saveAndBuildResponse(request, serviceType, response, config);
    }

    /**
     * Check payment status
     */
    public UnifiedPaymentResponse status(UnifiedStatusRequest request) {
        String serviceType = request.getServiceType().toUpperCase();
        log.info("Checking {} status for transaction: {}", serviceType, request.getTransactionId());

        PaymentGatewayTransaction transaction = findTransaction(request.getTransactionId());
        ThirdPartyIntegrationMaster config = getConfig(serviceType);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        requestBody.put("transactionId", transaction.getGatewayOrderId() != null
                ? transaction.getGatewayOrderId() : transaction.getTransactionId());
        requestBody.put("serviceType", serviceType);

        String url = config.getApiEndpoint() + "/api/v1/service/status";
        Map<String, Object> response = callApi(url, requestBody, config);

        // Update transaction
        String newStatus = extractString(response, "status", "UNKNOWN").toUpperCase();
        transaction.setStatus(newStatus);
        transaction.setGatewayResponse(response);

        if ("SUCCESS".equals(newStatus) || "COMPLETED".equals(newStatus)) {
            transaction.setPaymentReceivedAt(LocalDateTime.now());
            transaction.setGatewayPaymentId(extractString(response, "paymentId", "payment_id"));
        } else if ("FAILED".equals(newStatus)) {
            transaction.setFailureReason(extractString(response, "failureReason", "error", "message"));
        }

        paymentRepository.save(transaction);
        return buildResponseFromTransaction(transaction, serviceType, response);
    }

    /**
     * Cancel payment
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

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        requestBody.put("transactionId", transaction.getGatewayOrderId() != null
                ? transaction.getGatewayOrderId() : transaction.getTransactionId());
        requestBody.put("reason", request.getReason() != null ? request.getReason() : "Cancelled by user");
        requestBody.put("serviceType", serviceType);

        String url = config.getApiEndpoint() + "/api/v1/service/cancel";
        Map<String, Object> response = callApi(url, requestBody, config);

        transaction.setStatus("CANCELLED");
        transaction.setFailureReason(request.getReason());
        transaction.setGatewayResponse(response);
        paymentRepository.save(transaction);

        return buildResponseFromTransaction(transaction, serviceType, response);
    }

    /**
     * Refund payment
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
        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : transaction.getAmount();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        requestBody.put("originalTransactionId", transaction.getGatewayOrderId() != null
                ? transaction.getGatewayOrderId() : transaction.getTransactionId());
        requestBody.put("amount", refundAmount.toString());
        requestBody.put("merchantOrderId", transaction.getTransactionId());
        requestBody.put("message", request.getReason() != null ? request.getReason() : "Refund requested");
        requestBody.put("serviceType", serviceType);

        String url = config.getApiEndpoint() + "/api/v1/service/refund";
        Map<String, Object> response = callApi(url, requestBody, config);

        String refundStatus = extractString(response, "status", "UNKNOWN").toUpperCase();
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
                if (request.getInstrumentType() == null || request.getInstrumentType().isBlank()) {
                    throw new ApiCallException("Instrument type (VPA/MOBILE) is required for COLLECT_CALL");
                }
                if (request.getInstrumentReference() == null || request.getInstrumentReference().isBlank()) {
                    throw new ApiCallException("Instrument reference is required for COLLECT_CALL");
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

    private String getInitEndpoint(String serviceType) {
        return switch (serviceType) {
            case "DYNAMIC_QR" -> "/api/v1/dqr/init";
            case "COLLECT_CALL" -> "/api/v1/collect/collect-call";
            default -> "/api/v1/paymentLink/getPaymentLink";
        };
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
                if (request.getMessage() != null) body.put("message", request.getMessage());
            }
            case "COLLECT_CALL" -> {
                body.put("instrumentType", request.getInstrumentType());
                body.put("instrumentReference", request.getInstrumentReference());
            }
        }
        return body;
    }

    private UnifiedPaymentResponse initiateDqr(UnifiedPaymentRequest request, ThirdPartyIntegrationMaster config,
                                                Map<String, Object> requestBody) {
        String url = config.getApiEndpoint() + "/api/v1/dqr/init";
        try {
            byte[] qrImageBytes = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> addHeaders(h, config))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            String transactionId = UUID.randomUUID().toString();
            String qrBase64 = qrImageBytes != null ? Base64.getEncoder().encodeToString(qrImageBytes) : null;

            PaymentGatewayTransaction transaction = PaymentGatewayTransaction.builder()
                    .transactionId(transactionId)
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
                    .transactionId(transactionId)
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
        String gatewayOrderId = extractString(response, "orderId", "order_id", "transactionId");
        String paymentLink = extractString(response, "paymentLink", "payment_link", "url");
        String qrCodeUrl = extractString(response, "qrCodeUrl", "qr_code_url");

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
                .gatewayOrderId(transaction.getGatewayOrderId())
                .providerReferenceId(extractString(response, "providerReferenceId", "provider_reference_id"))
                .amount(request.getAmount())
                .currency("INR")
                .status("CREATED")
                .message(serviceType + " initiated successfully")
                .paymentLink(paymentLink)
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

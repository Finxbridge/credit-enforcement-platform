package com.finx.communication.service.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.communication.service.IntegrationCacheService;
import com.finx.communication.domain.dto.payment.*;
import com.finx.communication.domain.entity.PaymentGatewayTransaction;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.PaymentGatewayTransactionRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final PaymentGatewayTransactionRepository paymentRepository;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_INTEGRATION_NAME = "FINXBRIDGE_PAYMENT_LINK";
    private static final String DEFAULT_PROVIDER = "FINXBRIDGE";

    /**
     * Generate a payment link using FinxBridge API
     * Endpoint: POST /api/v1/paymentLink/getPaymentLink
     */
    public PaymentResponse generatePaymentLink(PaymentInitiateRequest request) {
        log.info("Generating payment link for amount: {} to mobile: {}", request.getAmount(), request.getMobileNumber());

        // 1. Get configuration from third_party_integration_master
        String integrationName = request.getGatewayName() != null
                ? request.getGatewayName() + "_PAYMENT_LINK"
                : DEFAULT_INTEGRATION_NAME;
        ThirdPartyIntegrationMaster config = getIntegrationConfig(integrationName);

        // 2. Build request body for FinxBridge API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("amount", request.getAmount().toString());
        requestBody.put("mobileNumber", request.getMobileNumber());
        requestBody.put("message", request.getMessage() != null ? request.getMessage() : "Payment for loan");
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        requestBody.put("storeId", config.getConfigValueAsString("store_id"));
        requestBody.put("terminalId", config.getConfigValueAsString("terminal_id"));

        // 3. Call FinxBridge Payment Link API
        String url = config.getApiEndpoint() + "/api/v1/paymentLink/getPaymentLink";
        Map<String, Object> response = callPaymentApi(url, requestBody, config);

        // 4. Parse response and save transaction
        String transactionId = UUID.randomUUID().toString();
        String paymentLink = extractPaymentLink(response);
        String qrCodeUrl = extractQrCodeUrl(response);
        String gatewayOrderId = extractGatewayOrderId(response);

        PaymentGatewayTransaction transaction = PaymentGatewayTransaction.builder()
                .transactionId(transactionId)
                .gatewayOrderId(gatewayOrderId)
                .gatewayName(request.getGatewayName() != null ? request.getGatewayName() : DEFAULT_PROVIDER)
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
        log.info("Payment link generated successfully. Transaction ID: {}", transactionId);

        return PaymentResponse.builder()
                .transactionId(transactionId)
                .gatewayOrderId(gatewayOrderId)
                .paymentLink(paymentLink)
                .qrCodeUrl(qrCodeUrl)
                .status("CREATED")
                .message("Payment link generated successfully")
                .amount(request.getAmount())
                .gatewayName(transaction.getGatewayName())
                .createdAt(transaction.getCreatedAt())
                .gatewayResponse(response)
                .build();
    }

    /**
     * Check payment status using FinxBridge API
     * Endpoint: POST /api/v1/service/status
     */
    public PaymentResponse checkPaymentStatus(PaymentStatusRequest request) {
        log.info("Checking payment status for transaction: {}", request.getTransactionId());

        // 1. Find transaction in database
        PaymentGatewayTransaction transaction = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ApiCallException("Transaction not found: " + request.getTransactionId()));

        // 2. Get configuration
        String integrationName = request.getGatewayName() != null
                ? request.getGatewayName() + "_PAYMENT_LINK"
                : transaction.getGatewayName() + "_PAYMENT_LINK";
        ThirdPartyIntegrationMaster config = getIntegrationConfig(integrationName);

        // 3. Build request body for status check
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        requestBody.put("transactionId", transaction.getGatewayOrderId() != null
                ? transaction.getGatewayOrderId()
                : transaction.getTransactionId());
        requestBody.put("serviceType", "PAYMENT_LINK");

        // 4. Call FinxBridge Status API
        String url = config.getApiEndpoint() + "/api/v1/service/status";
        Map<String, Object> response = callPaymentApi(url, requestBody, config);

        // 5. Update transaction status
        String newStatus = extractStatus(response);
        transaction.setStatus(newStatus);
        transaction.setGatewayResponse(response);

        if ("SUCCESS".equalsIgnoreCase(newStatus) || "COMPLETED".equalsIgnoreCase(newStatus)) {
            transaction.setPaymentReceivedAt(LocalDateTime.now());
            transaction.setGatewayPaymentId(extractGatewayPaymentId(response));
        } else if ("FAILED".equalsIgnoreCase(newStatus)) {
            transaction.setFailureReason(extractFailureReason(response));
        }

        paymentRepository.save(transaction);
        log.info("Payment status updated for transaction: {} - Status: {}", request.getTransactionId(), newStatus);

        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .gatewayOrderId(transaction.getGatewayOrderId())
                .gatewayTransactionId(transaction.getGatewayPaymentId())
                .paymentLink(transaction.getPaymentLink())
                .status(transaction.getStatus())
                .message("Payment status retrieved successfully")
                .amount(transaction.getAmount())
                .gatewayName(transaction.getGatewayName())
                .createdAt(transaction.getCreatedAt())
                .paidAt(transaction.getPaymentReceivedAt())
                .gatewayResponse(response)
                .build();
    }

    /**
     * Process refund using FinxBridge API
     * Endpoint: POST /api/v1/service/refund
     */
    public PaymentResponse processRefund(PaymentRefundRequest request) {
        log.info("Processing refund for transaction: {}", request.getTransactionId());

        // 1. Find transaction in database
        PaymentGatewayTransaction transaction = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ApiCallException("Transaction not found: " + request.getTransactionId()));

        // 2. Validate refund eligibility
        if (!"SUCCESS".equalsIgnoreCase(transaction.getStatus()) && !"COMPLETED".equalsIgnoreCase(transaction.getStatus())) {
            throw new ApiCallException("Cannot refund transaction with status: " + transaction.getStatus());
        }

        // 3. Get configuration
        String integrationName = request.getGatewayName() != null
                ? request.getGatewayName() + "_PAYMENT_LINK"
                : transaction.getGatewayName() + "_PAYMENT_LINK";
        ThirdPartyIntegrationMaster config = getIntegrationConfig(integrationName);

        // 4. Build request body for refund
        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : transaction.getAmount();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantId", config.getConfigValueAsString("merchant_id"));
        requestBody.put("originalTransactionId", transaction.getGatewayOrderId() != null
                ? transaction.getGatewayOrderId()
                : transaction.getTransactionId());
        requestBody.put("amount", refundAmount.toString());
        requestBody.put("merchantOrderId", transaction.getTransactionId());
        requestBody.put("message", request.getReason() != null ? request.getReason() : "Refund requested");
        requestBody.put("provider", config.getConfigValueAsString("provider"));
        requestBody.put("serviceType", "PAYMENT_LINK");

        // 5. Call FinxBridge Refund API
        String url = config.getApiEndpoint() + "/api/v1/service/refund";
        Map<String, Object> response = callPaymentApi(url, requestBody, config);

        // 6. Update transaction
        String refundStatus = extractStatus(response);
        if ("SUCCESS".equalsIgnoreCase(refundStatus) || "REFUNDED".equalsIgnoreCase(refundStatus)) {
            transaction.setStatus("REFUNDED");
            transaction.setRefundAmount(refundAmount);
            transaction.setRefundedAt(LocalDateTime.now());
        } else {
            transaction.setStatus("REFUND_" + refundStatus);
        }
        transaction.setGatewayResponse(response);

        paymentRepository.save(transaction);
        log.info("Refund processed for transaction: {} - Status: {}", request.getTransactionId(), transaction.getStatus());

        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .gatewayOrderId(transaction.getGatewayOrderId())
                .gatewayTransactionId(transaction.getGatewayPaymentId())
                .status(transaction.getStatus())
                .message("Refund processed successfully")
                .amount(transaction.getAmount())
                .refundAmount(transaction.getRefundAmount())
                .gatewayName(transaction.getGatewayName())
                .createdAt(transaction.getCreatedAt())
                .paidAt(transaction.getPaymentReceivedAt())
                .refundedAt(transaction.getRefundedAt())
                .gatewayResponse(response)
                .build();
    }

    /**
     * Get transaction details from database
     */
    public PaymentResponse getTransactionDetails(String transactionId) {
        log.info("Getting transaction details for: {}", transactionId);

        PaymentGatewayTransaction transaction = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ApiCallException("Transaction not found: " + transactionId));

        return mapTransactionToResponse(transaction);
    }

    /**
     * Get all transactions for a case
     */
    public List<PaymentResponse> getTransactionsByCase(Long caseId) {
        log.info("Getting transactions for case: {}", caseId);

        List<PaymentGatewayTransaction> transactions = paymentRepository.findByCaseId(caseId);

        return transactions.stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private ThirdPartyIntegrationMaster getIntegrationConfig(String integrationName) {
        return integrationCacheService.getIntegration(integrationName)
                .orElseThrow(() -> new ConfigurationNotFoundException(
                        "Integration configuration not found: " + integrationName));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callPaymentApi(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            log.debug("Calling payment API: {} with body: {}", url, body);

            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        // Add any required headers from config
                        String apiKey = config.getConfigValueAsString("api_key");
                        if (apiKey != null && !apiKey.isEmpty()) {
                            headers.set("X-API-Key", apiKey);
                        }
                        String authToken = config.getConfigValueAsString("auth_token");
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Payment API call failed: {}", error.getMessage()))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Payment API call failed", error)))
                    .block();

            log.debug("Payment API response: {}", response);

            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        } catch (ApiCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call payment API", e);
            throw new ApiCallException("Failed to call Payment API: " + e.getMessage(), e);
        }
    }

    private String extractPaymentLink(Map<String, Object> response) {
        if (response == null) return null;

        // Try different possible paths for payment link
        Object data = response.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object link = dataMap.get("paymentLink");
            if (link != null) return link.toString();
            link = dataMap.get("payment_link");
            if (link != null) return link.toString();
            link = dataMap.get("url");
            if (link != null) return link.toString();
        }

        Object link = response.get("paymentLink");
        if (link != null) return link.toString();
        link = response.get("payment_link");
        if (link != null) return link.toString();

        return null;
    }

    private String extractQrCodeUrl(Map<String, Object> response) {
        if (response == null) return null;

        Object data = response.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object qr = dataMap.get("qrCodeUrl");
            if (qr != null) return qr.toString();
            qr = dataMap.get("qr_code_url");
            if (qr != null) return qr.toString();
        }

        Object qr = response.get("qrCodeUrl");
        if (qr != null) return qr.toString();

        return null;
    }

    private String extractGatewayOrderId(Map<String, Object> response) {
        if (response == null) return null;

        Object data = response.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object orderId = dataMap.get("orderId");
            if (orderId != null) return orderId.toString();
            orderId = dataMap.get("order_id");
            if (orderId != null) return orderId.toString();
            orderId = dataMap.get("transactionId");
            if (orderId != null) return orderId.toString();
        }

        Object orderId = response.get("orderId");
        if (orderId != null) return orderId.toString();

        return UUID.randomUUID().toString();
    }

    private String extractGatewayPaymentId(Map<String, Object> response) {
        if (response == null) return null;

        Object data = response.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object paymentId = dataMap.get("paymentId");
            if (paymentId != null) return paymentId.toString();
            paymentId = dataMap.get("payment_id");
            if (paymentId != null) return paymentId.toString();
        }

        return null;
    }

    private String extractStatus(Map<String, Object> response) {
        if (response == null) return "UNKNOWN";

        Object data = response.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object status = dataMap.get("status");
            if (status != null) return status.toString().toUpperCase();
        }

        Object status = response.get("status");
        if (status != null) return status.toString().toUpperCase();

        return "UNKNOWN";
    }

    private String extractFailureReason(Map<String, Object> response) {
        if (response == null) return null;

        Object data = response.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object reason = dataMap.get("failureReason");
            if (reason != null) return reason.toString();
            reason = dataMap.get("error");
            if (reason != null) return reason.toString();
            reason = dataMap.get("message");
            if (reason != null) return reason.toString();
        }

        Object reason = response.get("error");
        if (reason != null) return reason.toString();
        reason = response.get("message");
        if (reason != null) return reason.toString();

        return null;
    }

    private PaymentResponse mapTransactionToResponse(PaymentGatewayTransaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .gatewayOrderId(transaction.getGatewayOrderId())
                .gatewayTransactionId(transaction.getGatewayPaymentId())
                .paymentLink(transaction.getPaymentLink())
                .status(transaction.getStatus())
                .message("Transaction details retrieved")
                .amount(transaction.getAmount())
                .refundAmount(transaction.getRefundAmount())
                .gatewayName(transaction.getGatewayName())
                .createdAt(transaction.getCreatedAt())
                .paidAt(transaction.getPaymentReceivedAt())
                .refundedAt(transaction.getRefundedAt())
                .gatewayResponse(transaction.getGatewayResponse())
                .build();
    }
}

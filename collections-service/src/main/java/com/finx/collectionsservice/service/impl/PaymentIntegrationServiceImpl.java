package com.finx.collectionsservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.collectionsservice.domain.dto.payment.*;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.service.PaymentIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Calls communication-service unified payment APIs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentIntegrationServiceImpl implements PaymentIntegrationService {

    @Qualifier("communicationServiceClient")
    private final WebClient communicationServiceClient;
    private final ObjectMapper objectMapper;
    private final com.finx.collectionsservice.service.RepaymentService repaymentService;
    private final com.finx.collectionsservice.service.ReceiptService receiptService;

    private static final String BASE_PATH = "/payments";

    @Override
    public PaymentResponse initiatePayment(PaymentInitRequest request) {
        log.info("Initiating {} payment for amount: ₹{}", request.getServiceType(), request.getAmount());

        // Convert amount from rupees to paisa (PhonePe expects amount in paisa)
        BigDecimal amountInPaisa = request.getAmount().multiply(new BigDecimal("100"));

        Map<String, Object> body = new HashMap<>();
        body.put("serviceType", request.getServiceType().name());
        body.put("amount", amountInPaisa); // Send amount in paisa

        // Add common tracking fields
        if (request.getCaseId() != null) body.put("caseId", request.getCaseId());
        if (request.getLoanAccountNumber() != null) body.put("loanAccountNumber", request.getLoanAccountNumber());
        if (request.getCustomerName() != null) body.put("customerName", request.getCustomerName());
        if (request.getCustomerEmail() != null) body.put("customerEmail", request.getCustomerEmail());

        // Add service-specific fields
        switch (request.getServiceType()) {
            case PAYMENT_LINK -> {
                if (request.getMobileNumber() != null) body.put("mobileNumber", request.getMobileNumber());
                if (request.getMessage() != null) body.put("message", request.getMessage());
            }
            case COLLECT_CALL -> {
                // instrumentType is always MOBILE from database config
                // Use instrumentReference if provided, otherwise use mobileNumber
                String mobileNumber = request.getInstrumentReference();
                if (mobileNumber == null || mobileNumber.isBlank()) {
                    mobileNumber = request.getMobileNumber();
                }
                if (mobileNumber != null) {
                    body.put("instrumentReference", mobileNumber);
                }
            }
            case DYNAMIC_QR -> {
                // No additional fields required for DQR
            }
        }

        log.debug("Sending payment request with amount in paisa: {}", amountInPaisa);
        JsonNode response = callApi(BASE_PATH + "/initiate", body);
        return mapResponse(response, request.getServiceType());
    }

    @Override
    public PaymentResponse checkStatus(PaymentStatusRequest request) {
        log.info("Checking {} status for: {}", request.getServiceType(), request.getTransactionId());

        Map<String, Object> body = new HashMap<>();
        body.put("serviceType", request.getServiceType().name());
        body.put("transactionId", request.getTransactionId());

        JsonNode response = callApi(BASE_PATH + "/status", body);
        PaymentResponse paymentResponse = mapResponse(response, request.getServiceType());

        // Auto-create repayment record when payment is SUCCESS
        // This triggers the database trigger to update loan_details.total_outstanding
        if ("SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            try {
                createRepaymentForSuccessfulPayment(paymentResponse, request.getTransactionId());
            } catch (Exception e) {
                // Log but don't fail - repayment creation is secondary to status check
                log.warn("Failed to auto-create repayment for successful payment {}: {}",
                    request.getTransactionId(), e.getMessage());
            }
        }

        return paymentResponse;
    }

    /**
     * Creates a repayment record for a successful digital payment.
     * This enables the database trigger to update loan_details.total_outstanding
     */
    private void createRepaymentForSuccessfulPayment(PaymentResponse payment, String transactionId) {
        // Check if repayment already exists for this transaction
        if (payment.getMerchantOrderId() != null) {
            try {
                repaymentService.getRepaymentByNumber(payment.getMerchantOrderId());
                log.debug("Repayment already exists for transaction {}", transactionId);
                return; // Already exists, skip creation
            } catch (Exception e) {
                // Repayment doesn't exist, continue to create
            }
        }

        // Validate we have caseId to link the repayment
        if (payment.getCaseId() == null) {
            log.warn("Cannot create repayment for transaction {} - no caseId", transactionId);
            return;
        }

        // Create repayment request with ONLINE/UPI mode (auto-approved)
        com.finx.collectionsservice.domain.dto.CreateRepaymentRequest repaymentRequest =
            com.finx.collectionsservice.domain.dto.CreateRepaymentRequest.builder()
                .caseId(payment.getCaseId())
                .paymentAmount(payment.getAmount())
                .paymentMode(mapPaymentMode(payment.getServiceType()))
                .notes("Digital payment via " + (payment.getServiceType() != null ? payment.getServiceType().name() : "online") +
                       ". Transaction ID: " + transactionId +
                       (payment.getMerchantOrderId() != null ? ". Order ID: " + payment.getMerchantOrderId() : ""))
                .paymentDate(payment.getPaidAt() != null ? payment.getPaidAt().toLocalDate() : java.time.LocalDate.now())
                .build();

        // System user ID for auto-created repayments
        Long systemUserId = 1L;

        com.finx.collectionsservice.domain.dto.RepaymentDTO createdRepayment =
            repaymentService.createRepayment(repaymentRequest, systemUserId);

        log.info("Auto-created repayment {} for successful payment {} - Status: {} (triggers loan update)",
            createdRepayment.getRepaymentNumber(), transactionId, createdRepayment.getApprovalStatus());
    }

    @Override
    public PaymentResponse cancelPayment(PaymentCancelRequest request) {
        log.info("Cancelling {} transaction: {}", request.getServiceType(), request.getTransactionId());

        Map<String, Object> body = new HashMap<>();
        body.put("serviceType", request.getServiceType().name());
        body.put("transactionId", request.getTransactionId());
        if (request.getReason() != null) body.put("reason", request.getReason());

        JsonNode response = callApi(BASE_PATH + "/cancel", body);
        return mapResponse(response, request.getServiceType());
    }

    @Override
    public PaymentResponse refundPayment(PaymentRefundRequest request) {
        log.info("Refunding {} transaction: {}", request.getServiceType(), request.getTransactionId());

        Map<String, Object> body = new HashMap<>();
        body.put("serviceType", request.getServiceType().name());
        body.put("transactionId", request.getTransactionId());
        if (request.getAmount() != null) body.put("amount", request.getAmount());
        if (request.getReason() != null) body.put("reason", request.getReason());

        JsonNode response = callApi(BASE_PATH + "/refund", body);
        return mapResponse(response, request.getServiceType());
    }

    @Override
    public PaymentResponse getTransaction(String transactionId) {
        JsonNode response = callGetApi(BASE_PATH + "/transaction/" + transactionId);
        return mapResponse(response, null);
    }

    @Override
    public List<PaymentResponse> getTransactionsByCase(Long caseId) {
        JsonNode response = callGetApi(BASE_PATH + "/case/" + caseId);
        List<PaymentResponse> result = new ArrayList<>();
        JsonNode data = response.path("data");
        if (data.isArray()) {
            for (JsonNode item : data) {
                result.add(mapJsonToResponse(item, null));
            }
        }
        return result;
    }

    // ==================== Private Methods ====================

    private JsonNode callApi(String endpoint, Map<String, Object> body) {
        try {
            String response = communicationServiceClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("API error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new BusinessException("Payment service error: " + ex.getMessage()));
                    })
                    .block();
            return objectMapper.readTree(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to call payment service: " + e.getMessage());
        }
    }

    private JsonNode callGetApi(String endpoint) {
        try {
            String response = communicationServiceClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("API error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new BusinessException("Payment service error: " + ex.getMessage()));
                    })
                    .block();
            return objectMapper.readTree(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to call payment service: " + e.getMessage());
        }
    }

    private PaymentResponse mapResponse(JsonNode response, com.finx.collectionsservice.domain.enums.PaymentServiceType serviceType) {
        // communication-service returns "payload" not "data"
        return mapJsonToResponse(response.path("payload"), serviceType);
    }

    private PaymentResponse mapJsonToResponse(JsonNode data, com.finx.collectionsservice.domain.enums.PaymentServiceType serviceType) {
        return PaymentResponse.builder()
                .serviceType(serviceType)
                .transactionId(getText(data, "transactionId"))
                .merchantOrderId(getText(data, "gatewayOrderId", "merchantOrderId"))
                .providerReferenceId(getText(data, "providerReferenceId"))
                .caseId(getLong(data, "caseId"))
                .loanAccountNumber(getText(data, "loanAccountNumber"))
                .amount(getDecimal(data, "amount"))
                .status(getText(data, "status"))
                .message(getText(data, "message"))
                .paymentLink(getText(data, "paymentLink"))
                .upiIntent(getText(data, "upiIntent"))
                .qrCodeBase64(getText(data, "qrCodeBase64"))
                .qrCodeUrl(getText(data, "qrCodeUrl"))
                .refundAmount(getDecimal(data, "refundAmount"))
                .createdAt(getDateTime(data, "createdAt"))
                .paidAt(getDateTime(data, "paidAt"))
                .refundedAt(getDateTime(data, "refundedAt"))
                .build();
    }

    private String getText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                return node.get(key).asText();
            }
        }
        return null;
    }

    private BigDecimal getDecimal(JsonNode node, String key) {
        if (node.has(key) && !node.get(key).isNull()) {
            return new BigDecimal(node.get(key).asText());
        }
        return null;
    }

    private Long getLong(JsonNode node, String key) {
        if (node.has(key) && !node.get(key).isNull()) {
            return node.get(key).asLong();
        }
        return null;
    }

    private LocalDateTime getDateTime(JsonNode node, String key) {
        if (node.has(key) && !node.get(key).isNull()) {
            try {
                return LocalDateTime.parse(node.get(key).asText());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public com.finx.collectionsservice.domain.dto.RepaymentDTO generateReceiptForTransaction(String transactionId, Long userId) {
        log.info("Generating receipt for transaction: {}", transactionId);

        // 1. Get transaction details from communication-service
        PaymentResponse payment = getTransaction(transactionId);

        // 2. Validate payment status
        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new BusinessException("Cannot generate receipt for non-successful payment. Current status: " + payment.getStatus());
        }

        // 3. Check if repayment already exists for this transaction
        com.finx.collectionsservice.domain.dto.RepaymentDTO existingRepayment = null;
        try {
            // Try to find existing repayment by checking case repayments
            if (payment.getMerchantOrderId() != null) {
                // Assuming merchantOrderId is stored in repayment reference or similar field
                existingRepayment = repaymentService.getRepaymentByNumber(payment.getMerchantOrderId());
            }
        } catch (Exception e) {
            // Repayment doesn't exist, we'll create it
            log.debug("No existing repayment found for transaction {}", transactionId);
        }

        // 4. Create repayment if it doesn't exist
        if (existingRepayment == null) {
            com.finx.collectionsservice.domain.dto.CreateRepaymentRequest repaymentRequest =
                com.finx.collectionsservice.domain.dto.CreateRepaymentRequest.builder()
                    .caseId(getCaseIdFromPayment(payment))
                    .paymentAmount(payment.getAmount())
                    .paymentMode(mapPaymentMode(payment.getServiceType()))
                    .notes("Digital payment via " + (payment.getServiceType() != null ? payment.getServiceType().name() : "online") +
                           ". Transaction ID: " + transactionId +
                           (payment.getMerchantOrderId() != null ? ". Order ID: " + payment.getMerchantOrderId() : ""))
                    .paymentDate(payment.getPaidAt() != null ? payment.getPaidAt().toLocalDate() : LocalDateTime.now().toLocalDate())
                    .build();

            existingRepayment = repaymentService.createRepayment(repaymentRequest, userId);
            log.info("Created repayment {} for transaction {} - Status: {}",
                existingRepayment.getId(), transactionId, existingRepayment.getApprovalStatus());

            // Digital payments are automatically approved during creation based on payment mode
        }

        // 5. Generate receipt if not already generated
        try {
            com.finx.collectionsservice.domain.dto.ReceiptDTO receipt = receiptService.getReceiptByRepaymentId(existingRepayment.getId());
            log.info("Receipt already exists: {}", receipt.getReceiptNumber());
        } catch (Exception e) {
            // Receipt doesn't exist, generate it
            receiptService.generateReceipt(existingRepayment.getId(), userId);
            log.info("Generated receipt for repayment {}", existingRepayment.getId());
        }

        return existingRepayment;
    }

    @Override
    public byte[] downloadReceiptForTransaction(String transactionId, Long userId) {
        log.info("Downloading receipt for transaction: {}", transactionId);

        // 1. Get or create repayment with receipt
        com.finx.collectionsservice.domain.dto.RepaymentDTO repayment = generateReceiptForTransaction(transactionId, userId);

        // 2. Get receipt for the repayment
        com.finx.collectionsservice.domain.dto.ReceiptDTO receipt = receiptService.getReceiptByRepaymentId(repayment.getId());

        // 3. Download receipt PDF
        return receiptService.downloadReceiptPdf(receipt.getId(), userId);
    }

    private Long getCaseIdFromPayment(PaymentResponse payment) {
        // Extract caseId from payment response
        return payment.getCaseId();
    }

    private com.finx.collectionsservice.domain.enums.PaymentMode mapPaymentMode(com.finx.collectionsservice.domain.enums.PaymentServiceType serviceType) {
        if (serviceType == null) {
            return com.finx.collectionsservice.domain.enums.PaymentMode.ONLINE;
        }
        switch (serviceType) {
            case DYNAMIC_QR:
            case COLLECT_CALL:
                return com.finx.collectionsservice.domain.enums.PaymentMode.UPI;
            case PAYMENT_LINK:
                return com.finx.collectionsservice.domain.enums.PaymentMode.ONLINE;
            default:
                return com.finx.collectionsservice.domain.enums.PaymentMode.ONLINE;
        }
    }
}

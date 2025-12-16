package com.finx.communication.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified payment response for all service types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedPaymentResponse {

    private String serviceType;
    private String transactionId;
    private String gatewayOrderId;
    private String gatewayTransactionId;
    private String providerReferenceId;

    private Long caseId;
    private String loanAccountNumber;

    private BigDecimal amount;
    private String currency;
    private String status;
    private String message;

    // PAYMENT_LINK specific
    private String paymentLink;

    // DYNAMIC_QR specific
    private String qrCodeBase64;
    private String qrCodeUrl;

    // Refund info
    private BigDecimal refundAmount;
    private String failureReason;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime expiresAt;

    // Raw gateway response
    private Map<String, Object> gatewayResponse;
}

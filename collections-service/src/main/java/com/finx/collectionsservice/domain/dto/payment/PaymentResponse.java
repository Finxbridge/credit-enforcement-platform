package com.finx.collectionsservice.domain.dto.payment;

import com.finx.collectionsservice.domain.enums.PaymentServiceType;
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
public class PaymentResponse {

    private PaymentServiceType serviceType;
    private String transactionId;
    private String merchantOrderId;
    private String providerReferenceId;
    private Long caseId;
    private String loanAccountNumber;
    private BigDecimal amount;
    private String status;
    private String message;

    // Payment Link specific
    private String paymentLink;

    // DQR specific
    private String qrCodeBase64;
    private String qrCodeUrl;

    // Refund info
    private BigDecimal refundAmount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime expiresAt;

    // Raw gateway response (for debugging)
    private Map<String, Object> gatewayResponse;
}

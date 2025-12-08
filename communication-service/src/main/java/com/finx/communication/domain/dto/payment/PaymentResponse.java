package com.finx.communication.domain.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private String transactionId;
    private String gatewayOrderId;
    private String gatewayTransactionId;
    private String paymentLink;
    private String qrCodeUrl;
    private String status;
    private String message;
    private BigDecimal amount;
    private BigDecimal refundAmount;
    private String gatewayName;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private Map<String, Object> gatewayResponse;
}

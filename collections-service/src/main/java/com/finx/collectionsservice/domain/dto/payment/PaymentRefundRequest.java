package com.finx.collectionsservice.domain.dto.payment;

import com.finx.collectionsservice.domain.enums.PaymentServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request to refund a payment transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {

    @NotNull(message = "Service type is required")
    private PaymentServiceType serviceType;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    // Optional - if not provided, full refund
    private BigDecimal amount;

    private String reason;
}

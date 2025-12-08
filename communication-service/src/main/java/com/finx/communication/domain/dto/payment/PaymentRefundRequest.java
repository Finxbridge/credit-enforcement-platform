package com.finx.communication.domain.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    // Optional - if not provided, full refund will be processed
    private BigDecimal amount;

    private String reason;

    // Optional - will be fetched from third_party_integration_master if not provided
    private String gatewayName;
}

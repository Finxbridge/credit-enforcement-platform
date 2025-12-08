package com.finx.communication.domain.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    // Optional - will be fetched from third_party_integration_master if not provided
    private String gatewayName;
}

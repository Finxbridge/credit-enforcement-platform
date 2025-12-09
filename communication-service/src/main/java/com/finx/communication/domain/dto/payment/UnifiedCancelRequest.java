package com.finx.communication.domain.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified cancel request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCancelRequest {

    @NotBlank(message = "Service type is required (DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL)")
    private String serviceType;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    private String reason;
}

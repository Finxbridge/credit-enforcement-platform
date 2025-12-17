package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request for partial payment adjustment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartialPaymentRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Partial payment amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal partialAmount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String paymentMode;
    private String transactionId;
    private String notes;

    // Outstanding balance tracking
    private BigDecimal previousOutstanding;
    private BigDecimal newOutstanding;
}

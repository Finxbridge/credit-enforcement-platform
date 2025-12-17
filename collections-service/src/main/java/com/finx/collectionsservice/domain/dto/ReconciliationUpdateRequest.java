package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request to update reconciliation status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationUpdateRequest {

    @NotNull(message = "Repayment ID is required")
    private Long repaymentId;

    @NotNull(message = "Reconciliation status is required")
    private String reconciliationStatus; // SUCCESS, FAILED, MISMATCH

    private String mismatchReason;
    private BigDecimal actualAmount;
    private String notes;
}

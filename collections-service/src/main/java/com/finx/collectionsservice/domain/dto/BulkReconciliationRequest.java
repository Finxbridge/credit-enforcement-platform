package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for bulk reconciliation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkReconciliationRequest {

    @NotEmpty(message = "Repayment IDs are required")
    private List<Long> repaymentIds;

    private String reconciliationBatchId;
    private String notes;
}

package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for reconciliation records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationDTO {

    private Long repaymentId;
    private String repaymentNumber;
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private BigDecimal paymentAmount;
    private String paymentMode;
    private String transactionId;

    // Reconciliation Status
    private String reconciliationStatus; // PENDING, SUCCESS, FAILED, MISMATCH
    private Boolean isReconciled;
    private LocalDateTime reconciledAt;
    private Long reconciledBy;
    private String reconciledByName;
    private String reconciliationBatchId;

    // Mismatch Details
    private String mismatchReason;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;

    private LocalDateTime createdAt;
}

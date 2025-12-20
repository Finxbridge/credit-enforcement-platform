package com.finx.strategyengineservice.domain.enums;

/**
 * Available date filter fields for strategy rules
 * Maps to entity field paths (e.g., "loan.dueDate" for Due Date field in LoanDetails)
 * Updated to match unified CSV format
 */
public enum DateFilterField {

    // ==================== LOAN DATES ====================
    DATE_OF_DISBURSEMENT("Date of Disbursement", "loan.loanDisbursementDate"),
    MATURITY_DATE("Maturity Date", "loan.loanMaturityDate"),
    DUE_DATE("Due Date", "loan.dueDate"),
    WRITEOFF_DATE("Writeoff Date", "loan.writeoffDate"),

    // ==================== EMI DATES ====================
    EMI_START_DATE("EMI Start Date", "loan.emiStartDate"),
    EMI_OVERDUE_FROM("EMI Overdue From", "loan.emiOverdueFrom"),
    NEXT_EMI_DATE("Next EMI Date", "loan.nextEmiDate"),

    // ==================== CREDIT CARD DATES ====================
    STATEMENT_DATE("Statement Date", "loan.statementDate"),

    // ==================== PAYMENT DATES ====================
    LAST_PAYMENT_DATE("Last Payment Date", "loan.lastPaymentDate"),

    // ==================== BLOCK DATES ====================
    BLOCK_1_DATE("Block 1 Date", "loan.block1Date"),
    BLOCK_2_DATE("Block 2 Date", "loan.block2Date"),

    // ==================== PTP DATES ====================
    PTP_DATE("PTP Date", "ptpDate"),
    NEXT_FOLLOWUP_DATE("Next Followup Date", "nextFollowupDate"),

    // ==================== CASE DATES ====================
    CASE_OPENED_AT("Case Opened At", "caseOpenedAt"),
    CASE_CLOSED_AT("Case Closed At", "caseClosedAt"),
    ALLOCATED_AT("Allocated At", "allocatedAt");

    private final String displayName;
    private final String fieldPath;

    DateFilterField(String displayName, String fieldPath) {
        this.displayName = displayName;
        this.fieldPath = fieldPath;
    }

    DateFilterField(String displayName) {
        this.displayName = displayName;
        this.fieldPath = null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFieldPath() {
        return fieldPath;
    }
}

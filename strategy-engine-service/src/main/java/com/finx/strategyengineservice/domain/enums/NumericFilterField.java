package com.finx.strategyengineservice.domain.enums;

/**
 * Available numeric filter fields for strategy rules
 * Maps to entity field paths (e.g., "loan.dpd" for DPD field in LoanDetails)
 * Updated to match unified CSV format
 */
public enum NumericFilterField {

    // ==================== LOAN AMOUNTS ====================
    OVERDUE_AMOUNT("Overdue Amount", "loan.totalOutstanding"),
    LOAN_AMOUNT("Loan Amount or Limit", "loan.loanAmount"),
    POS("POS (Principal Outstanding)", "loan.pos"),
    TOS("TOS (Total Outstanding)", "loan.tos"),
    EMI_AMOUNT("EMI Amount", "loan.emiAmount"),
    PENALTY_AMOUNT("Penalty Amount", "loan.penaltyAmount"),
    CHARGES("Charges", "loan.charges"),
    OD_INTEREST("OD Interest", "loan.odInterest"),

    // ==================== OVERDUE BREAKDOWN ====================
    PRINCIPAL_OVERDUE("Principal Overdue", "loan.principalOverdue"),
    INTEREST_OVERDUE("Interest Overdue", "loan.interestOverdue"),
    FEES_OVERDUE("Fees Overdue", "loan.feesOverdue"),
    PENALTY_OVERDUE("Penalty Overdue", "loan.penaltyOverdue"),

    // ==================== EMI COUNTS ====================
    PAID_EMI_COUNT("Number of Paid EMI", "loan.noOfPaidEmi"),
    PENDING_EMI_COUNT("Number of Pending EMI", "loan.noOfPendingEmi"),

    // ==================== DPD ====================
    DPD("DPD", "loan.dpd"),
    SOM_DPD("SOM DPD", "loan.somDpd"),

    // ==================== CREDIT CARD ====================
    MINIMUM_AMOUNT_DUE("Minimum Amount Due", "loan.minimumAmountDue"),
    CARD_OUTSTANDING("Card Outstanding", "loan.cardOutstanding"),
    LAST_BILLED_AMOUNT("Last Billed Amount", "loan.lastBilledAmount"),

    // ==================== PAYMENT ====================
    LAST_PAID_AMOUNT("Last Paid Amount", "loan.lastPaidAmount"),

    // ==================== RATES ====================
    ROI("Rate of Interest", "loan.roi"),
    INTEREST_RATE("Interest Rate", "loan.interestRate"),
    TENURE_MONTHS("Tenure in Months", "loan.tenureMonths"),

    // ==================== PTP ====================
    PTP_AMOUNT("PTP Amount", "ptpAmount");

    private final String displayName;
    private final String fieldPath;

    NumericFilterField(String displayName, String fieldPath) {
        this.displayName = displayName;
        this.fieldPath = fieldPath;
    }

    NumericFilterField(String displayName) {
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

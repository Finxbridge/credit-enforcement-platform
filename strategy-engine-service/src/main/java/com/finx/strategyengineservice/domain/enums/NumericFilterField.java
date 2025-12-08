package com.finx.strategyengineservice.domain.enums;

/**
 * Available numeric filter fields
 */
public enum NumericFilterField {
    OVERDUE_AMOUNT("Overdue Amount"),
    LOAN_AMOUNT("Loan Amount or Limit"),
    POS("POS (Principal Outstanding)"),
    EMI_AMOUNT("EMI Amount"),
    PAID_EMI_COUNT("Number of Paid EMI"),
    PENDING_EMI_COUNT("Number of Pending EMI"),
    PENALTY_AMOUNT("Penalty Amount"),
    CHARGES("Charges"),
    LATE_FEES("Late Fees"),
    OD_INTEREST("OD Interest"),
    MINIMUM_AMOUNT_DUE("Minimum Amount Due"),
    CARD_OUTSTANDING("Card Outstanding"),
    LAST_BILLED_AMOUNT("Last Billed Amount"),
    LAST_PAID_AMOUNT("Last Paid Amount"),
    DPD("DPD"),
    BUREAU_SCORE("Bureau Score"),
    PRINCIPAL_OVERDUE("Principal Overdue"),
    INTEREST_OVERDUE("Interest Overdue"),
    FEES_OVERDUE("Fees Overdue"),
    PENALTY_OVERDUE("Penalty Overdue"),
    DAYS_FROM_LAST_CYCLE("Days from Last Cycle");

    private final String displayName;

    NumericFilterField(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

package com.finx.strategyengineservice.domain.enums;

/**
 * Available date filter fields
 */
public enum DateFilterField {
    DATE_OF_DISBURSEMENT("Date of Disbursement"),
    MATURITY_DATE("Maturity Date"),
    STATEMENT_DATE("Statement Date"),
    DUE_DATE("Due Date"),
    LAST_PAYMENT_DATE("Last Payment Date"),
    EMI_OVERDUE_FROM("EMI Overdue From"),
    NEXT_EMI_DATE("Next EMI Date");

    private final String displayName;

    DateFilterField(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

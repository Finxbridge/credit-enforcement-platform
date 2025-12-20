package com.finx.strategyengineservice.domain.enums;

/**
 * Types of filters available in the strategy engine
 */
public enum FilterType {
    TEXT,       // Language, Product, State, Pincode
    NUMERIC,    // Outstanding amount, DPD, EMI amount, etc.
    DATE        // Disbursement date, Due date, Payment date, etc.
}

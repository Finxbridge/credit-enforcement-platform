package com.finx.collectionsservice.domain.enums;

/**
 * Types of closure rules for case archival
 */
public enum ClosureRuleType {
    /**
     * Close cases where outstanding amount is zero
     */
    ZERO_OUTSTANDING,

    /**
     * Close cases that are fully settled via OTS
     */
    FULLY_SETTLED,

    /**
     * Close cases that have been written off
     */
    WRITTEN_OFF,

    /**
     * Close cases with no activity for specified days
     */
    NO_ACTIVITY,

    /**
     * Close cases marked as NPA and aged
     */
    NPA_AGED,

    /**
     * Custom rule with specific criteria
     */
    CUSTOM
}

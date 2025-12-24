package com.finx.collectionsservice.domain.enums;

/**
 * Status of rule execution
 */
public enum RuleExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    PARTIALLY_COMPLETED,
    FAILED,
    CANCELLED
}

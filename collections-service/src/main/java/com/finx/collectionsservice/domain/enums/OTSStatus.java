package com.finx.collectionsservice.domain.enums;

/**
 * OTS (One-Time Settlement) Status Enumeration
 * Tracks the lifecycle of an OTS request
 */
public enum OTSStatus {
    /**
     * Initial intent captured from customer
     */
    INTENT_CAPTURED,

    /**
     * Request pending approval
     */
    PENDING_APPROVAL,

    /**
     * Request approved by authorized personnel
     */
    APPROVED,

    /**
     * Request rejected
     */
    REJECTED,

    /**
     * Settlement letter generated
     */
    LETTER_GENERATED,

    /**
     * Awaiting payment from customer
     */
    PAYMENT_PENDING,

    /**
     * Partial payment received
     */
    PARTIAL_PAID,

    /**
     * Settlement completed
     */
    SETTLED,

    /**
     * OTS cancelled
     */
    CANCELLED,

    /**
     * OTS offer expired
     */
    EXPIRED
}

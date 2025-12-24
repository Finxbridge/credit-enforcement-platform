package com.finx.collectionsservice.domain.enums;

/**
 * OTS (One-Time Settlement) Status Enumeration
 * Tracks the lifecycle of an OTS request
 */
public enum OTSStatus {
    /**
     * Request pending approval
     */
    PENDING_APPROVAL,

    /**
     * Request approved - Settlement letter generated
     */
    APPROVED,

    /**
     * Request rejected
     */
    REJECTED,

    /**
     * OTS offer expired (payment deadline passed)
     */
    EXPIRED
}

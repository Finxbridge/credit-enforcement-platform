package com.finx.casesourcingservice.domain.enums;

/**
 * PTP (Promise to Pay) Status Enumeration
 * Tracks the lifecycle of a PTP commitment
 */
public enum PTPStatus {
    /**
     * PTP commitment made, awaiting payment
     */
    PENDING,

    /**
     * Payment received as promised
     */
    KEPT,

    /**
     * Payment not received by due date
     */
    BROKEN,

    /**
     * PTP renewed with new date/amount
     */
    RENEWED,

    /**
     * Partial payment received (less than committed amount)
     */
    PARTIAL,

    /**
     * PTP cancelled by user
     */
    CANCELLED
}

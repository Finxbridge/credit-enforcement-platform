package com.finx.strategyengineservice.domain.enums;

/**
 * Enum for communication status tracking
 */
public enum CommunicationStatus {
    PENDING,
    QUEUED,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    CANCELLED,
    // Notice-specific statuses
    GENERATED,
    DISPATCHED,
    IN_TRANSIT,
    RTO
}

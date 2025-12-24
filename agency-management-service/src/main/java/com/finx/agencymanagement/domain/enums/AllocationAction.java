package com.finx.agencymanagement.domain.enums;

/**
 * Enum representing allocation actions for the unified allocation history.
 */
public enum AllocationAction {
    // Primary allocation actions (from allocation-reallocation-service)
    ALLOCATED,
    REALLOCATED,
    DEALLOCATED,
    BULK_REALLOCATION,
    AGENT_TRANSFER,
    RULE_BASED_ALLOCATION,

    // Agency management actions
    AGENCY_ALLOCATED,       // Case allocated to an agency
    AGENCY_DEALLOCATED,     // Case deallocated from an agency
    AGENT_ASSIGNED,         // Case assigned to an agent within agency
    AGENT_REASSIGNED,       // Case reassigned from one agent to another
    AGENT_UNASSIGNED        // Case unassigned from agent
}

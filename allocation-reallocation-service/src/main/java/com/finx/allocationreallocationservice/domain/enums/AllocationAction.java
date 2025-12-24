package com.finx.allocationreallocationservice.domain.enums;

public enum AllocationAction {
    // Primary allocation actions
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

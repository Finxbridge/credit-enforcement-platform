package com.finx.allocationreallocationservice.domain.dto;

import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating allocation history entries from external services.
 * Used by agency-management-service to record agency/agent allocations in the unified history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAllocationHistoryRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    private String externalCaseId;

    @NotNull(message = "Action is required")
    private AllocationAction action;

    /**
     * User ID the case was allocated/assigned TO (can be agent or agency user)
     */
    private Long allocatedToUserId;

    private String allocatedToUsername;

    /**
     * Type of the new owner: USER, AGENCY, AGENT
     */
    @Builder.Default
    private String newOwnerType = "USER";

    /**
     * User ID the case was allocated/assigned FROM (previous owner)
     */
    private Long allocatedFromUserId;

    /**
     * Type of previous owner: USER, AGENCY, AGENT
     */
    private String previousOwnerType;

    /**
     * Reason for the action (deallocation reason, reassignment reason, etc.)
     */
    private String reason;

    /**
     * User who performed the action
     */
    @NotNull(message = "Allocated by user ID is required")
    private Long allocatedBy;

    /**
     * Batch ID for bulk operations
     */
    private String batchId;

    /**
     * Agency ID (for agency allocation tracking)
     */
    private Long agencyId;

    /**
     * Agency code (for display purposes)
     */
    private String agencyCode;

    /**
     * Agency name (for display purposes)
     */
    private String agencyName;
}

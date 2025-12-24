package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agency Case Allocation DTO
 * Matches frontend agency.types.ts AgencyCaseAllocation interface
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyCaseAllocationDTO {

    private Long id;

    @JsonProperty("agencyId")
    private Long agencyId;

    @JsonProperty("agencyName")
    private String agencyName;

    @JsonProperty("agencyCode")
    private String agencyCode;

    @JsonProperty("caseId")
    private Long caseId;

    @JsonProperty("externalCaseId")
    private String externalCaseId;

    @JsonProperty("agentId")
    private Long agentId;

    @JsonProperty("agentName")
    private String agentName;

    @JsonProperty("allocationStatus")
    private String allocationStatus;

    @JsonProperty("assignmentStatus")
    private String assignmentStatus; // UNALLOCATED, ALLOCATED_TO_AGENCY, ASSIGNED_TO_AGENT

    @JsonProperty("batchId")
    private String batchId;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("allocatedAt")
    private LocalDateTime allocatedAt;

    @JsonProperty("allocatedBy")
    private Long allocatedBy;

    @JsonProperty("deallocatedAt")
    private LocalDateTime deallocatedAt;

    @JsonProperty("deallocatedBy")
    private Long deallocatedBy;

    @JsonProperty("deallocatedReason")
    private String deallocatedReason;

    // For showing all assignments of a case
    @JsonProperty("assignments")
    private List<CaseAssignmentInfo> assignments;

    @JsonProperty("assignmentCount")
    private Integer assignmentCount;

    /**
     * Inner class to hold assignment info for a case
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseAssignmentInfo {
        private Long agencyId;
        private String agencyName;
        private String agencyCode;
        private Long agentId;
        private String agentName;
        private LocalDateTime assignedAt;
    }
}

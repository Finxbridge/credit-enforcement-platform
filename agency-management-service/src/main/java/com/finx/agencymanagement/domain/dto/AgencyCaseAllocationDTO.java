package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @JsonProperty("caseId")
    private Long caseId;

    @JsonProperty("externalCaseId")
    private String externalCaseId;

    @JsonProperty("agentId")
    private Long agentId;

    @JsonProperty("allocationStatus")
    private String allocationStatus;

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
}

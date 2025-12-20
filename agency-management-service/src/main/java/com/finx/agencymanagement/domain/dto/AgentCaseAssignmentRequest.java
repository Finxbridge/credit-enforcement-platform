package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for assigning cases from agency to agent
 * Second level allocation - from agency to specific agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentCaseAssignmentRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Case IDs are required")
    private List<Long> caseIds;

    private String notes;
}

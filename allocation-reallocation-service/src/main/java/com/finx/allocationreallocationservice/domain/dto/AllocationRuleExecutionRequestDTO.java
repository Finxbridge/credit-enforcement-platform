package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRuleExecutionRequestDTO {
    @NotEmpty(message = "Agent IDs are required")
    private List<Long> agentIds;

    // Only required for PERCENTAGE_SPLIT rule type
    private List<Integer> percentages;

    // Optional - specific case IDs to allocate (from simulate response)
    // If not provided, all cases matching rule criteria will be allocated
    private List<Long> caseIds;

    // Optional - limit number of cases to allocate
    private Integer maxCases;
}

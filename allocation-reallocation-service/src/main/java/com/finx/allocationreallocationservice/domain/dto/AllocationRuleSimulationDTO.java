package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRuleSimulationDTO {
    private Long ruleId;
    private Integer unallocatedCases;
    private List<Long> caseIds;  // List of case IDs that match the rule criteria
    private List<Long> agentIds;  // List of eligible agent IDs (ready to use in apply API)
    private List<EligibleAgentDTO> eligibleAgents;
    private Map<String, Integer> suggestedDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligibleAgentDTO {
        private Long agentId;
        private String agentName;
        private Integer capacity;
        private Integer currentWorkload;
        private Integer availableCapacity;
    }

    // Keep old structure for backward compatibility
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulatedAllocationDTO {
        private Long userId;
        private Long casesCount;
    }
}

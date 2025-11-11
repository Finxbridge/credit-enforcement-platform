package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRuleSimulationDTO {
    private Long ruleId;
    private Long estimatedCases;
    private List<SimulatedAllocationDTO> simulatedAllocations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulatedAllocationDTO {
        private Long userId;
        private Long casesCount;
    }
}

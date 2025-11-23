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
public class AllocationRuleExecutionResponseDTO {
    private Long ruleId;
    private Integer totalCasesAllocated;
    private List<AllocationResultDTO> allocations;
    private String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationResultDTO {
        private Long agentId;
        private Integer allocated;
    }
}

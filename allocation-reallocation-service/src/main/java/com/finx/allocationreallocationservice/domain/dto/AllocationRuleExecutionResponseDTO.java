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
    private String executionId;
    private Long ruleId;
    private String ruleName;
    private Integer totalCasesAllocated;
    private Boolean dryRun;
    private String status;
    private List<AllocationResultDTO> allocationResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationResultDTO {
        private Long userId;
        private String username;
        private Integer casesAllocated;
    }
}

package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Cycle Closure Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleClosureDashboardDTO {

    private Long totalEligibleForArchival;
    private Long totalArchivedThisCycle;
    private Long totalArchivedAllTime;
    private Integer activeRulesCount;
    private Long pendingClosures;
    private Long failedClosures;
    private Map<String, Long> archivalByReason;
    private List<RecentExecutionDTO> recentExecutions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentExecutionDTO {
        private String executionId;
        private String ruleName;
        private Integer casesArchived;
        private String status;
        private String executedAt;
    }
}

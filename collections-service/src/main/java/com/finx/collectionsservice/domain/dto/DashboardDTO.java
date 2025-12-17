package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Dashboard DTOs for Collections Service
 */
public class DashboardDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleClosureDashboard {
        private Long totalEligibleForArchival;
        private Long totalArchivedThisCycle;
        private Long totalArchivedAllTime;
        private Long activeRulesCount;
        private Long pendingClosures;
        private Long failedClosures;
        private Map<String, Long> archivalByReason;
        private java.util.List<RecentExecution> recentExecutions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentExecution {
        private String executionId;
        private String ruleName;
        private Integer casesArchived;
        private String status;
        private java.time.LocalDateTime executedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepaymentDashboard {
        private BigDecimal totalCollectedToday;
        private BigDecimal totalCollectedThisMonth;
        private Long totalTransactionsToday;
        private Long totalTransactionsThisMonth;
        private Long pendingApprovals;
        private Long slaBreaches;
        private Map<String, BigDecimal> collectionByPaymentMode;
        private Map<String, Long> transactionsByStatus;
        private BigDecimal averageTransactionAmount;
        private Double approvalRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OTSDashboard {
        private Long totalRequests;
        private Long pendingApprovals;
        private Long approvedThisMonth;
        private Long rejectedThisMonth;
        private Long settledThisMonth;
        private BigDecimal totalSettledAmount;
        private BigDecimal totalWaiverAmount;
        private Double averageDiscountPercentage;
        private Double approvalRate;
        private Long expiredRequests;
        private Map<String, Long> requestsByStatus;
        private Long lettersGenerated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionsDashboard {
        private CycleClosureDashboard cycleClosureDashboard;
        private RepaymentDashboard repaymentDashboard;
        private OTSDashboard otsDashboard;
        private PTPStatsDTO ptpStats;
    }
}

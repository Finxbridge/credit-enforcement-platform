package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dashboard item for individual strategy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyDashboardItem {

    private Long strategyId;
    private String strategyName;
    private LocalDateTime lastRun;
    private LocalDateTime nextRun;
    private String channel;  // SMS, WHATSAPP, EMAIL, IVR, NOTICE
    private Double successRate;
    private Integer totalExecutions;
    private Integer successCount;
    private Integer failureCount;
    private String status;  // ACTIVE, INACTIVE, DRAFT
    private Boolean isSchedulerEnabled;
    private Integer priority;
    private String frequency;  // DAILY, WEEKLY, EVENT_BASED
}

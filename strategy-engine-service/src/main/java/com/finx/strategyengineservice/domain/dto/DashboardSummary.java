package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard summary with overall statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    private Integer totalStrategies;
    private Integer activeStrategies;
    private Integer inactiveStrategies;
    private Integer draftStrategies;
    private Long totalExecutions;
    private Double overallSuccessRate;
    private Integer enabledSchedulers;
}

package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete dashboard response with summary and strategy list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private DashboardSummary summary;
    private List<StrategyDashboardItem> strategies;
}

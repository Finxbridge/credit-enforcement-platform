package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.dto.DashboardResponse;
import com.finx.strategyengineservice.domain.dto.SimulationResponse;
import com.finx.strategyengineservice.domain.dto.StrategyRequest;
import com.finx.strategyengineservice.domain.dto.StrategyResponse;

import java.util.List;

/**
 * Service for unified strategy management
 * Handles complete strategy creation/update in a single operation
 */
public interface StrategyService {

    /**
     * Create complete strategy with all configurations
     *
     * @param request Unified strategy request with filters, template, schedule
     * @return Complete strategy response
     */
    StrategyResponse createStrategy(StrategyRequest request);

    /**
     * Update complete strategy configuration
     *
     * @param strategyId Strategy ID
     * @param request Updated configuration
     * @return Updated strategy response
     */
    StrategyResponse updateStrategy(Long strategyId, StrategyRequest request);

    /**
     * Get complete strategy details
     *
     * @param strategyId Strategy ID
     * @return Complete strategy response
     */
    StrategyResponse getStrategy(Long strategyId);

    /**
     * Get all strategies with optional status filter
     *
     * @param status Filter by status (DRAFT/ACTIVE/INACTIVE), null for all
     * @return List of strategies
     */
    List<StrategyResponse> getAllStrategies(String status);

    /**
     * Delete strategy and all related data
     *
     * @param strategyId Strategy ID
     */
    void deleteStrategy(Long strategyId);

    /**
     * Update strategy status
     *
     * @param strategyId Strategy ID
     * @param status New status (DRAFT/ACTIVE/INACTIVE)
     * @return Updated strategy
     */
    StrategyResponse updateStrategyStatus(Long strategyId, String status);

    /**
     * Simulate strategy to see matched cases and count
     *
     * @param strategyId Strategy ID
     * @return Simulation response with matched cases and count
     */
    SimulationResponse simulateStrategy(Long strategyId);

    /**
     * Enable/Disable scheduler for strategy
     *
     * @param strategyId Strategy ID
     * @param enabled Enable or disable
     * @return Updated strategy
     */
    StrategyResponse toggleScheduler(Long strategyId, Boolean enabled);

    /**
     * Get dashboard metrics with summary and strategy list
     *
     * @return Dashboard response with overall statistics and strategy details
     */
    DashboardResponse getDashboardMetrics();
}

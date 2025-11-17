package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.entity.ScheduledJob;

/**
 * Service for managing strategy scheduler
 */
public interface StrategySchedulerService {

    /**
     * Enable scheduler for a strategy
     *
     * @param strategyId Strategy ID
     * @return Created/Updated scheduled job
     */
    ScheduledJob enableScheduler(Long strategyId);

    /**
     * Disable scheduler for a strategy
     *
     * @param strategyId Strategy ID
     */
    void disableScheduler(Long strategyId);

    /**
     * Update scheduler configuration for a strategy
     *
     * @param strategyId Strategy ID
     * @return Updated scheduled job
     */
    ScheduledJob updateSchedulerConfig(Long strategyId);

    /**
     * Get scheduler status for a strategy
     *
     * @param strategyId Strategy ID
     * @return Scheduled job if exists
     */
    ScheduledJob getSchedulerStatus(Long strategyId);

    /**
     * Execute all due scheduled strategies
     * Called by @Scheduled method every minute
     */
    void executeDueStrategies();

    /**
     * Calculate next run time for a scheduled job
     *
     * @param scheduledJob Scheduled job
     * @return Next run timestamp
     */
    java.time.LocalDateTime calculateNextRunTime(ScheduledJob scheduledJob);
}

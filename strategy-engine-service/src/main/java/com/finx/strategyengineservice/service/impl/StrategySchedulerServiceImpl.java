package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.entity.ScheduledJob;
import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.enums.ScheduleStatus;
import com.finx.strategyengineservice.domain.enums.ScheduleType;
import com.finx.strategyengineservice.exception.ResourceNotFoundException;
import com.finx.strategyengineservice.repository.ScheduledJobRepository;
import com.finx.strategyengineservice.repository.StrategyRepository;
import com.finx.strategyengineservice.service.StrategyExecutionService;
import com.finx.strategyengineservice.service.StrategySchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

/**
 * Implementation of Strategy Scheduler Service
 * Manages automated strategy execution based on schedule configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategySchedulerServiceImpl implements StrategySchedulerService {

    private final ScheduledJobRepository scheduledJobRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyExecutionService executionService;

    private static final String SERVICE_NAME = "strategy-engine-service";
    private static final String JOB_TYPE = "STRATEGY_EXECUTION";
    private static final String REFERENCE_TYPE = "STRATEGY";

    @SuppressWarnings("null")
    @Override
    @Transactional
    public ScheduledJob enableScheduler(Long strategyId) {
        log.info("Enabling scheduler for strategy: {}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        // Check if scheduler already exists
        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElseGet(() -> createScheduledJob(strategy));

        // Enable the scheduler
        scheduledJob.setIsEnabled(true);

        // Calculate next run time
        LocalDateTime nextRun = calculateNextRunTime(scheduledJob);
        scheduledJob.setNextRunAt(nextRun);
        scheduledJob.setUpdatedAt(LocalDateTime.now());

        scheduledJobRepository.save(scheduledJob);

        log.info("Scheduler enabled for strategy {}. Next run at: {}", strategyId, nextRun);
        return scheduledJob;
    }

    @Override
    @Transactional
    public void disableScheduler(Long strategyId) {
        log.info("Disabling scheduler for strategy: {}", strategyId);

        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Scheduled job not found for strategy: " + strategyId));

        scheduledJob.setIsEnabled(false);
        scheduledJob.setUpdatedAt(LocalDateTime.now());
        scheduledJobRepository.save(scheduledJob);

        log.info("Scheduler disabled for strategy: {}", strategyId);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    public ScheduledJob updateSchedulerConfig(Long strategyId) {
        log.info("Updating scheduler config for strategy: {}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Scheduled job not found for strategy: " + strategyId));

        // Update schedule configuration from strategy
        updateScheduledJobFromStrategy(scheduledJob, strategy);

        // Recalculate next run time
        if (scheduledJob.getIsEnabled()) {
            LocalDateTime nextRun = calculateNextRunTime(scheduledJob);
            scheduledJob.setNextRunAt(nextRun);
        }

        scheduledJob.setUpdatedAt(LocalDateTime.now());
        scheduledJobRepository.save(scheduledJob);

        log.info("Scheduler config updated for strategy: {}", strategyId);
        return scheduledJob;
    }

    @Override
    public ScheduledJob getSchedulerStatus(Long strategyId) {
        return scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElse(null);
    }

    /**
     * Scheduled method - runs every minute to check for due strategies
     */
    @Scheduled(cron = "0 * * * * *") // Run every minute
    @Transactional
    public void executeDueStrategies() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Checking for due strategies at: {}", now);

        List<ScheduledJob> dueJobs = scheduledJobRepository.findDueJobs(now, SERVICE_NAME);

        if (dueJobs.isEmpty()) {
            log.debug("No due strategies found");
            return;
        }

        log.info("Found {} due strategies to execute", dueJobs.size());

        for (ScheduledJob job : dueJobs) {
            try {
                executeScheduledStrategy(job);
            } catch (Exception e) {
                log.error("Failed to execute scheduled strategy {}: {}", job.getJobReferenceId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Execute a single scheduled strategy
     */
    private void executeScheduledStrategy(ScheduledJob job) {
        Long strategyId = job.getJobReferenceId();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("Executing scheduled strategy: {} (Job: {})", strategyId, job.getId());

        // Mark job as RUNNING
        job.setLastRunStatus(ScheduleStatus.RUNNING);
        job.setLastRunAt(startTime);
        scheduledJobRepository.save(job);

        try {
            // Execute the strategy
            executionService.executeStrategy(strategyId);

            // Mark as SUCCESS
            LocalDateTime endTime = LocalDateTime.now();
            long executionTime = Duration.between(startTime, endTime).toMillis();

            job.setLastRunStatus(ScheduleStatus.SUCCESS);
            job.setLastRunMessage("Execution completed successfully");
            job.setRunCount(job.getRunCount() + 1);

            // Update average execution time
            updateAverageExecutionTime(job, executionTime);

            // Calculate next run time
            LocalDateTime nextRun = calculateNextRunTime(job);
            job.setNextRunAt(nextRun);
            job.setUpdatedAt(LocalDateTime.now());

            scheduledJobRepository.save(job);

            log.info("Strategy {} executed successfully. Next run: {}", strategyId, nextRun);

        } catch (Exception e) {
            log.error("Strategy {} execution failed: {}", strategyId, e.getMessage(), e);

            // Mark as FAILED
            job.setLastRunStatus(ScheduleStatus.FAILED);
            job.setLastRunMessage("Execution failed: " + e.getMessage());
            job.setFailureCount(job.getFailureCount() + 1);

            // Still calculate next run time for retry
            LocalDateTime nextRun = calculateNextRunTime(job);
            job.setNextRunAt(nextRun);
            job.setUpdatedAt(LocalDateTime.now());

            scheduledJobRepository.save(job);
        }
    }

    @Override
    public LocalDateTime calculateNextRunTime(ScheduledJob scheduledJob) {
        if (!scheduledJob.getIsEnabled()) {
            return null;
        }

        ScheduleType scheduleType = scheduledJob.getScheduleType();
        LocalDateTime now = LocalDateTime.now();
        ZoneId timezone = ZoneId.of(scheduledJob.getTimezone());

        switch (scheduleType) {
            case DAILY:
                return calculateDailyNextRun(scheduledJob, now, timezone);

            case WEEKLY:
                return calculateWeeklyNextRun(scheduledJob, now, timezone);

            case EVENT_BASED:
                // For event-based, no automatic scheduling
                return null;

            default:
                log.warn("Unsupported schedule type: {}", scheduleType);
                return null;
        }
    }

    /**
     * Calculate next run for DAILY schedule
     */
    private LocalDateTime calculateDailyNextRun(ScheduledJob job, LocalDateTime now, ZoneId timezone) {
        LocalTime scheduleTime = job.getScheduleTime();

        if (scheduleTime == null) {
            log.warn("Schedule time not set for DAILY job: {}", job.getId());
            return now.plusDays(1);
        }

        LocalDateTime nextRun = LocalDateTime.of(now.toLocalDate(), scheduleTime);

        // If today's scheduled time has passed, move to tomorrow
        if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
            nextRun = nextRun.plusDays(1);
        }

        return nextRun;
    }

    /**
     * Calculate next run for WEEKLY schedule
     */
    private LocalDateTime calculateWeeklyNextRun(ScheduledJob job, LocalDateTime now, ZoneId timezone) {
        LocalTime scheduleTime = job.getScheduleTime();
        String scheduleDays = job.getScheduleDays();

        if (scheduleTime == null || scheduleDays == null || scheduleDays.isEmpty()) {
            log.warn("Schedule time or days not set for WEEKLY job: {}", job.getId());
            return now.plusDays(7);
        }

        // Parse schedule days (e.g., "MONDAY,WEDNESDAY,FRIDAY")
        String[] days = scheduleDays.split(",");

        // Find next scheduled day
        for (int i = 0; i < 14; i++) { // Check up to 2 weeks
            LocalDateTime candidate = now.plusDays(i);
            DayOfWeek candidateDay = candidate.getDayOfWeek();

            for (String day : days) {
                if (candidateDay.name().equalsIgnoreCase(day.trim())) {
                    LocalDateTime nextRun = LocalDateTime.of(candidate.toLocalDate(), scheduleTime);

                    // Ensure it's in the future
                    if (nextRun.isAfter(now)) {
                        return nextRun;
                    }
                }
            }
        }

        // Fallback: next week same day
        return now.plusDays(7);
    }

    /**
     * Create scheduled job from strategy
     */
    private ScheduledJob createScheduledJob(Strategy strategy) {
        ScheduledJob job = new ScheduledJob();
        job.setServiceName(SERVICE_NAME);
        job.setJobName(strategy.getStrategyName());
        job.setJobType(JOB_TYPE);
        job.setJobReferenceId(strategy.getId());
        job.setJobReferenceType(REFERENCE_TYPE);
        job.setIsEnabled(false); // Default disabled

        updateScheduledJobFromStrategy(job, strategy);

        return job;
    }

    /**
     * Update scheduled job fields from strategy
     */
    private void updateScheduledJobFromStrategy(ScheduledJob job, Strategy strategy) {
        // Determine schedule type from strategy
        if (strategy.getTriggerFrequency() != null) {
            switch (strategy.getTriggerFrequency().toUpperCase()) {
                case "DAILY":
                    job.setScheduleType(ScheduleType.DAILY);
                    job.setScheduleTime(strategy.getTriggerTime());
                    break;

                case "WEEKLY":
                    job.setScheduleType(ScheduleType.WEEKLY);
                    job.setScheduleTime(strategy.getTriggerTime());
                    job.setScheduleDays(strategy.getTriggerDays());
                    break;

                case "EVENT_BASED":
                case "EVENT":
                    job.setScheduleType(ScheduleType.EVENT_BASED);
                    break;

                default:
                    job.setScheduleType(ScheduleType.DAILY);
                    break;
            }
        }

        // Set cron expression if available
        if (strategy.getScheduleExpression() != null && !strategy.getScheduleExpression().isEmpty()) {
            job.setCronExpression(strategy.getScheduleExpression());
        }

        job.setJobName(strategy.getStrategyName());
    }

    /**
     * Update average execution time
     */
    private void updateAverageExecutionTime(ScheduledJob job, long executionTime) {
        Long currentAvg = job.getAvgExecutionTimeMs();
        Integer runCount = job.getRunCount();

        if (currentAvg == null || runCount == 0) {
            job.setAvgExecutionTimeMs(executionTime);
        } else {
            // Calculate rolling average
            long newAvg = ((currentAvg * runCount) + executionTime) / (runCount + 1);
            job.setAvgExecutionTimeMs(newAvg);
        }
    }
}

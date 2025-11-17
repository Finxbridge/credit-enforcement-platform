package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.ScheduledJob;
import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ScheduledJob (Common scheduler table)
 */
@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {

    /**
     * Find scheduled job by reference (e.g., strategy_id)
     */
    Optional<ScheduledJob> findByJobReferenceTypeAndJobReferenceId(String referenceType, Long referenceId);

    /**
     * Find all enabled jobs that are due to run, ordered by strategy priority (higher priority first)
     */
    @Query("SELECT j FROM ScheduledJob j " +
           "JOIN Strategy s ON j.jobReferenceId = s.id " +
           "WHERE j.isEnabled = TRUE " +
           "AND j.nextRunAt IS NOT NULL " +
           "AND j.nextRunAt <= :now " +
           "AND j.serviceName = :serviceName " +
           "AND j.jobReferenceType = 'STRATEGY' " +
           "ORDER BY s.priority DESC, j.nextRunAt ASC")
    List<ScheduledJob> findDueJobs(@Param("now") LocalDateTime now,
                                     @Param("serviceName") String serviceName);

    /**
     * Find all enabled jobs for a service
     */
    List<ScheduledJob> findByServiceNameAndIsEnabled(String serviceName, Boolean isEnabled);

    /**
     * Find jobs by service and type
     */
    List<ScheduledJob> findByServiceNameAndJobType(String serviceName, String jobType);

    /**
     * Update job status after execution
     */
    @Modifying
    @Query("UPDATE ScheduledJob j SET j.lastRunAt = :lastRunAt, " +
           "j.lastRunStatus = :status, " +
           "j.lastRunMessage = :message, " +
           "j.runCount = j.runCount + 1, " +
           "j.updatedAt = :now " +
           "WHERE j.id = :jobId")
    void updateJobAfterSuccess(@Param("jobId") Long jobId,
                                @Param("lastRunAt") LocalDateTime lastRunAt,
                                @Param("status") ScheduleStatus status,
                                @Param("message") String message,
                                @Param("now") LocalDateTime now);

    /**
     * Update job status after failure
     */
    @Modifying
    @Query("UPDATE ScheduledJob j SET j.lastRunAt = :lastRunAt, " +
           "j.lastRunStatus = :status, " +
           "j.lastRunMessage = :message, " +
           "j.failureCount = j.failureCount + 1, " +
           "j.updatedAt = :now " +
           "WHERE j.id = :jobId")
    void updateJobAfterFailure(@Param("jobId") Long jobId,
                                 @Param("lastRunAt") LocalDateTime lastRunAt,
                                 @Param("status") ScheduleStatus status,
                                 @Param("message") String message,
                                 @Param("now") LocalDateTime now);

    /**
     * Update next run time
     */
    @Modifying
    @Query("UPDATE ScheduledJob j SET j.nextRunAt = :nextRunAt, j.updatedAt = :now WHERE j.id = :jobId")
    void updateNextRunTime(@Param("jobId") Long jobId,
                           @Param("nextRunAt") LocalDateTime nextRunAt,
                           @Param("now") LocalDateTime now);

    /**
     * Enable/disable job
     */
    @Modifying
    @Query("UPDATE ScheduledJob j SET j.isEnabled = :enabled, j.updatedAt = :now WHERE j.id = :jobId")
    void updateJobEnabled(@Param("jobId") Long jobId,
                          @Param("enabled") Boolean enabled,
                          @Param("now") LocalDateTime now);

    /**
     * Count enabled jobs for a service
     */
    Long countByServiceNameAndIsEnabled(String serviceName, Boolean isEnabled);
}

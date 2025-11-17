package com.finx.strategyengineservice.domain.entity;

import com.finx.strategyengineservice.domain.enums.ScheduleStatus;
import com.finx.strategyengineservice.domain.enums.ScheduleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * Common Scheduled Job entity
 * Platform-wide scheduler table used across all microservices
 * Manages scheduled jobs for Strategy Execution, Batch Imports, Auto-Allocation, etc.
 */
@Entity
@Table(name = "scheduled_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "job_name", nullable = false, length = 255)
    private String jobName;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(name = "job_reference_id")
    private Long jobReferenceId;

    @Column(name = "job_reference_type", length = 50)
    private String jobReferenceType;

    @Column(name = "is_enabled")
    private Boolean isEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private ScheduleType scheduleType;

    @Column(name = "schedule_time")
    private LocalTime scheduleTime;

    @Column(name = "schedule_days", length = 100)
    private String scheduleDays;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Kolkata";

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 20)
    private ScheduleStatus lastRunStatus;

    @Column(name = "last_run_message", columnDefinition = "TEXT")
    private String lastRunMessage;

    @Column(name = "run_count")
    private Integer runCount = 0;

    @Column(name = "failure_count")
    private Integer failureCount = 0;

    @Column(name = "avg_execution_time_ms")
    private Long avgExecutionTimeMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_config", columnDefinition = "jsonb")
    private Map<String, Object> jobConfig;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

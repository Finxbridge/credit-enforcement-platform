package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Closure Rule Entity
 * Defines rules for automatic case closure/archival
 */
@Entity
@Table(name = "closure_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosureRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", unique = true, nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private ClosureRuleType ruleType;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "is_scheduled")
    private Boolean isScheduled;

    @Column(name = "closure_reason", nullable = false, length = 100)
    private String closureReason;

    // Rule criteria as JSON
    @Column(name = "criteria", columnDefinition = "TEXT")
    private String criteria;

    // Minimum days with zero outstanding before closure
    @Column(name = "min_zero_outstanding_days")
    private Integer minZeroOutstandingDays;

    // Minimum days of inactivity before closure
    @Column(name = "min_inactivity_days")
    private Integer minInactivityDays;

    // Include only specific buckets (comma-separated: X,1,2,3)
    @Column(name = "include_buckets", length = 50)
    private String includeBuckets;

    // Exclude specific case statuses (comma-separated)
    @Column(name = "exclude_statuses", length = 200)
    private String excludeStatuses;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "last_execution_count")
    private Integer lastExecutionCount;

    @Column(name = "total_cases_closed")
    private Long totalCasesClosed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isScheduled == null) {
            isScheduled = false;
        }
        if (priority == null) {
            priority = 10;
        }
        if (totalCasesClosed == null) {
            totalCasesClosed = 0L;
        }
        if (minZeroOutstandingDays == null) {
            minZeroOutstandingDays = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

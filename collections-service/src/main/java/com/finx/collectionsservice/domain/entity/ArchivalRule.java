package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.RuleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Archival Rule Entity
 * Defines rules for auto-archiving cases based on criteria
 */
@Entity
@Table(name = "archival_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Store JSON as TEXT - criteria for archiving cases
    @Column(name = "criteria", columnDefinition = "TEXT")
    private String criteria;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "schedule_description", length = 255)
    private String scheduleDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RuleStatus status;

    @Column(name = "execution_count")
    private Integer executionCount;

    @Column(name = "last_execution_at")
    private LocalDateTime lastExecutionAt;

    @Column(name = "last_execution_result", length = 20)
    private String lastExecutionResult;

    @Column(name = "last_cases_archived")
    private Integer lastCasesArchived;

    @Column(name = "next_execution_at")
    private LocalDateTime nextExecutionAt;

    @Column(name = "total_cases_archived")
    private Long totalCasesArchived;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = RuleStatus.ACTIVE;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (executionCount == null) {
            executionCount = 0;
        }
        if (totalCasesArchived == null) {
            totalCasesArchived = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

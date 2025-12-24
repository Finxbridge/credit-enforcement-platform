package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.RuleExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Closure Rule Execution Entity
 * Tracks each execution of a closure rule
 */
@Entity
@Table(name = "closure_rule_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosureRuleExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_id", unique = true, nullable = false, length = 100)
    private String executionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private ClosureRule rule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private RuleExecutionStatus status;

    @Column(name = "is_simulation")
    private Boolean isSimulation;

    @Column(name = "total_eligible")
    private Integer totalEligible;

    @Column(name = "total_processed")
    private Integer totalProcessed;

    @Column(name = "total_success")
    private Integer totalSuccess;

    @Column(name = "total_failed")
    private Integer totalFailed;

    @Column(name = "total_skipped")
    private Integer totalSkipped;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_log", columnDefinition = "TEXT")
    private String executionLog;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = RuleExecutionStatus.PENDING;
        }
        if (isSimulation == null) {
            isSimulation = false;
        }
        if (totalEligible == null) {
            totalEligible = 0;
        }
        if (totalProcessed == null) {
            totalProcessed = 0;
        }
        if (totalSuccess == null) {
            totalSuccess = 0;
        }
        if (totalFailed == null) {
            totalFailed = 0;
        }
        if (totalSkipped == null) {
            totalSkipped = 0;
        }
    }
}

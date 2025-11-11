package com.finx.strategyengineservice.domain.entity;

import com.finx.strategyengineservice.domain.enums.ExecutionStatus;
import com.finx.strategyengineservice.domain.enums.ExecutionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "strategy_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_id", unique = true, length = 100)
    private String executionId;

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId;

    @Column(name = "strategy_name", length = 255)
    private String strategyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", length = 20)
    private ExecutionType executionType;

    @Column(name = "total_records_evaluated")
    @Builder.Default
    private Integer totalRecordsEvaluated = 0;

    @Column(name = "records_matched")
    @Builder.Default
    private Integer recordsMatched = 0;

    @Column(name = "records_processed")
    @Builder.Default
    private Integer recordsProcessed = 0;

    @Column(name = "records_failed")
    @Builder.Default
    private Integer recordsFailed = 0;

    @Column(name = "successful_actions")
    @Builder.Default
    private Integer successfulActions = 0;

    @Column(name = "failed_actions")
    @Builder.Default
    private Integer failedActions = 0;

    @Column(name = "estimated_cases_affected")
    private Integer estimatedCasesAffected;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", length = 20)
    @Builder.Default
    private ExecutionStatus executionStatus = ExecutionStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_log", columnDefinition = "jsonb")
    private List<Map<String, Object>> executionLog;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_metadata", columnDefinition = "jsonb")
    private Map<String, Object> executionMetadata;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (executionId == null) {
            executionId = "exec_" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to get total cases processed (maps to old field)
    public Integer getTotalCasesProcessed() {
        return recordsProcessed;
    }

    public void setTotalCasesProcessed(Integer count) {
        this.recordsProcessed = count;
    }
}

package com.finx.strategyengineservice.domain.entity;

import com.finx.strategyengineservice.domain.enums.StrategyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "strategies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_code", unique = true, nullable = false, length = 50)
    private String strategyCode;

    @Column(name = "strategy_name", nullable = false, length = 255)
    private String strategyName;

    @Column(name = "strategy_type", nullable = false, length = 20)
    private String strategyType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "trigger_frequency", length = 20)
    private String triggerFrequency;

    @Column(name = "trigger_time")
    private LocalTime triggerTime;

    @Column(name = "trigger_days", length = 255)
    private String triggerDays;

    @Column(name = "schedule_expression", length = 100)
    private String scheduleExpression;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private StrategyStatus status = StrategyStatus.DRAFT;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "success_count")
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;

    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StrategyRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StrategyAction> actions = new ArrayList<>();

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
        if (strategyCode == null) {
            strategyCode = "STR_" + System.currentTimeMillis();
        }
        if (strategyType == null) {
            strategyType = "COLLECTION";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for managing relationships
    public void addRule(StrategyRule rule) {
        rules.add(rule);
        rule.setStrategy(this);
    }

    public void removeRule(StrategyRule rule) {
        rules.remove(rule);
        rule.setStrategy(null);
    }

    public void addAction(StrategyAction action) {
        actions.add(action);
        action.setStrategy(this);
    }

    public void removeAction(StrategyAction action) {
        actions.remove(action);
        action.setStrategy(null);
    }
}

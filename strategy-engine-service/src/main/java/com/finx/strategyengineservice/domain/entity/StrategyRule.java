package com.finx.strategyengineservice.domain.entity;

import com.finx.strategyengineservice.domain.enums.RuleOperator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "strategy_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "rule_order")
    @Builder.Default
    private Integer ruleOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> conditions;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", length = 50)
    private RuleOperator operator;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    @Column(name = "logical_operator", length = 5)
    @Builder.Default
    private String logicalOperator = "AND";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (ruleName == null && fieldName != null) {
            ruleName = fieldName + "_" + (operator != null ? operator.name() : "RULE");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

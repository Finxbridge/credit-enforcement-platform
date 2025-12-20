package com.finx.noticemanagementservice.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "routing_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", unique = true, nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "rule_priority")
    private Integer rulePriority;

    @Type(JsonType.class)
    @Column(name = "criteria", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> criteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_vendor_id")
    private NoticeVendor primaryVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_vendor_id")
    private NoticeVendor secondaryVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fallback_vendor_id")
    private NoticeVendor fallbackVendor;

    @Column(name = "dispatch_sla_hours")
    private Integer dispatchSlaHours;

    @Column(name = "delivery_sla_days")
    private Integer deliverySlaDays;

    @Column(name = "max_cost_per_dispatch", precision = 10, scale = 2)
    private BigDecimal maxCostPerDispatch;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

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
        if (rulePriority == null) {
            rulePriority = 0;
        }
        if (dispatchSlaHours == null) {
            dispatchSlaHours = 24;
        }
        if (deliverySlaDays == null) {
            deliverySlaDays = 7;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

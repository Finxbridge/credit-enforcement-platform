package com.finx.noticemanagementservice.domain.entity;

import com.finx.noticemanagementservice.domain.enums.BreachSeverity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_breaches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaBreach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "breach_id", unique = true, nullable = false, length = 100)
    private String breachId;

    @Column(name = "breach_type", nullable = false, length = 30)
    private String breachType; // DISPATCH_SLA, DELIVERY_SLA, RESPONSE_SLA

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_reference", length = 100)
    private String entityReference;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "expected_by", nullable = false)
    private LocalDateTime expectedBy;

    @Column(name = "breached_at", nullable = false)
    private LocalDateTime breachedAt;

    @Column(name = "breach_duration_hours")
    private Integer breachDurationHours;

    // Responsible Party
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(name = "assigned_user_name", length = 100)
    private String assignedUserName;

    // Escalation
    @Enumerated(EnumType.STRING)
    @Column(name = "breach_severity", length = 20)
    private BreachSeverity breachSeverity;

    @Column(name = "is_escalated")
    private Boolean isEscalated;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_to")
    private Long escalatedTo;

    @Column(name = "escalated_by")
    private Long escalatedBy;

    @Column(name = "escalation_level")
    private Integer escalationLevel;

    @Column(name = "escalation_notes", columnDefinition = "TEXT")
    private String escalationNotes;

    // Resolution
    @Column(name = "is_resolved")
    private Boolean isResolved;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolution_action", length = 50)
    private String resolutionAction;

    @Column(name = "penalty_amount", precision = 10, scale = 2)
    private BigDecimal penaltyAmount;

    // Audit
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (breachSeverity == null) {
            breachSeverity = BreachSeverity.LOW;
        }
        if (isEscalated == null) {
            isEscalated = false;
        }
        if (escalationLevel == null) {
            escalationLevel = 0;
        }
        if (isResolved == null) {
            isResolved = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

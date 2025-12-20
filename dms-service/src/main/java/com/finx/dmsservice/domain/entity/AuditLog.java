package com.finx.dmsservice.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audit_id", length = 50, unique = true)
    private String auditId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Type(JsonType.class)
    @Column(name = "before_value", columnDefinition = "jsonb")
    private Map<String, Object> beforeValue;

    @Type(JsonType.class)
    @Column(name = "after_value", columnDefinition = "jsonb")
    private Map<String, Object> afterValue;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    @Type(JsonType.class)
    @Column(name = "changed_fields", columnDefinition = "jsonb")
    private List<String> changedFields;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Transient fields for service compatibility (not persisted)
    @Transient
    private String serviceName;

    @Transient
    private String eventType;

    @Transient
    private String eventCategory;

    @Transient
    private String entityName;

    @Transient
    private Map<String, Object> oldValue;

    @Transient
    private Map<String, Object> newValue;

    @Transient
    private String changeSummary;

    @Transient
    private Long actorId;

    @Transient
    private String actorName;

    @Transient
    private String actorType;

    @Transient
    private String traceId;

    @Transient
    private String severity;

    @Transient
    private LocalDateTime eventTimestamp;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Map transient fields to persisted fields
        if (oldValue != null && beforeValue == null) {
            beforeValue = oldValue;
        }
        if (newValue != null && afterValue == null) {
            afterValue = newValue;
        }
        if (actorId != null && userId == null) {
            userId = actorId;
        }
        if (actorName != null && userName == null) {
            userName = actorName;
        }
        if (changeSummary != null && description == null) {
            description = changeSummary;
        }
    }
}

package com.finx.myworkflow.domain.entity;

import com.finx.myworkflow.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_case", columnList = "case_id"),
        @Index(name = "idx_audit_timestamp", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audit_id", nullable = false, unique = true, length = 50)
    private String auditId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    // Store JSON as TEXT - will be serialized/deserialized in service layer
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (auditId == null) {
            auditId = "AUD-" + System.currentTimeMillis() + "-" +
                    java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}

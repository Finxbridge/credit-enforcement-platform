package com.finx.agencymanagement.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * Agency Audit Log Entity
 * Tracks all agency-related actions for audit trail
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Entity
@Table(name = "agency_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_type", length = 30)
    private String actorType = "USER";

    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Type(JsonType.class)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    @Type(JsonType.class)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Type(JsonType.class)
    @Column(name = "changed_fields", columnDefinition = "jsonb")
    private String changedFields;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

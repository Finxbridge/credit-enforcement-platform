package com.finx.dmsservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_category", length = 30)
    private String eventCategory;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Column(name = "actor_type", length = 30)
    private String actorType;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "trace_id", length = 50)
    private String traceId;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @PrePersist
    protected void onCreate() {
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
        if (serviceName == null) {
            serviceName = "dms-service";
        }
    }
}

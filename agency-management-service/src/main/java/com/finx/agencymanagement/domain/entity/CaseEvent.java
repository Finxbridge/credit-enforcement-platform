package com.finx.agencymanagement.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for case_events table - tracks all agency allocation events.
 */
@Entity
@Table(name = "case_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    private String eventId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number")
    private String loanAccountNumber;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_subtype")
    private String eventSubtype;

    @Column(name = "event_category", nullable = false)
    private String eventCategory;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "actor_type")
    private String actorType;

    @Column(name = "source_service")
    private String sourceService;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "communication_channel")
    private String communicationChannel;

    @Column(name = "communication_status")
    private String communicationStatus;

    @Column(name = "communication_id")
    private Long communicationId;

    @Column(name = "from_agent_id")
    private Long fromAgentId;

    @Column(name = "to_agent_id")
    private Long toAgentId;

    @Column(name = "ptp_amount")
    private BigDecimal ptpAmount;

    @Column(name = "ptp_date")
    private LocalDate ptpDate;

    @Column(name = "ptp_status")
    private String ptpStatus;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
        if (eventId == null) {
            eventId = "EVT-" + System.currentTimeMillis() + "-" +
                    java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}

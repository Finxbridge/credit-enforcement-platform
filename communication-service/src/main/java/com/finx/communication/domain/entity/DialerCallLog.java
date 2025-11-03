package com.finx.communication.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for tracking Dialer call logs
 */
@Entity
@Table(name = "dialer_call_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", unique = true, nullable = false, length = 100)
    private String callId;

    @Column(name = "dialer_call_id", length = 100)
    private String dialerCallId; // Ozonetel call ID

    @Builder.Default
    @Column(name = "dialer_name", length = 50)
    private String dialerName = "OZONETEL";

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "customer_mobile", nullable = false, length = 15)
    private String customerMobile;

    @Column(name = "call_type", nullable = false, length = 20)
    private String callType; // CLICK_TO_CALL, AUTO_DIAL, INBOUND

    @Column(name = "call_status", length = 50)
    private String callStatus; // INITIATED, RINGING, ANSWERED, BUSY, FAILED, NO_ANSWER

    @Column(name = "call_duration")
    private Integer callDuration; // in seconds

    @Column(name = "recording_url", columnDefinition = "TEXT")
    private String recordingUrl;

    @Column(name = "disposition", length = 100)
    private String disposition;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "queue_priority")
    private Integer queuePriority;

    @Column(name = "queued_at")
    private LocalDateTime queuedAt;

    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Type(JsonType.class)
    @Column(name = "dialer_response", columnDefinition = "jsonb")
    private Map<String, Object> dialerResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

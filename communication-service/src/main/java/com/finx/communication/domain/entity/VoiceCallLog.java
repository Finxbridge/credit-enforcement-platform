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
 * Entity for tracking MSG91 Voice call logs
 */
@Entity
@Table(name = "voice_call_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", unique = true, nullable = false, length = 100)
    private String callId;

    @Column(name = "provider_call_id", length = 100)
    private String providerCallId; // MSG91 call ID

    @Builder.Default
    @Column(name = "provider", length = 50)
    private String provider = "MSG91";

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_mobile", nullable = false, length = 15)
    private String customerMobile;

    @Column(name = "caller_id", length = 15)
    private String callerId;

    @Column(name = "call_type", nullable = false, length = 30)
    private String callType; // CLICK_TO_CALL, VOICE_SMS

    @Column(name = "template", length = 100)
    private String template; // For voice SMS

    @Column(name = "call_status", length = 50)
    private String callStatus; // INITIATED, COMPLETED, FAILED

    @Column(name = "call_duration")
    private Integer callDuration; // in seconds

    @Column(name = "recording_url", columnDefinition = "TEXT")
    private String recordingUrl;

    @Column(name = "disposition", length = 100)
    private String disposition;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Type(JsonType.class)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Map<String, Object> providerResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

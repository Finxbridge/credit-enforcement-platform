package com.finx.communication.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking SMS messages
 */
@Entity
@Table(name = "sms_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false, length = 100)
    private String messageId;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_code", length = 50)
    private String templateCode;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Builder.Default
    @Column(name = "sender_id", length = 20)
    private String senderId = "FINXCO";

    @Builder.Default
    @Column(name = "provider", length = 50)
    private String provider = "MSG91";

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "QUEUED"; // QUEUED, SENT, DELIVERED, FAILED, REJECTED

    @Builder.Default
    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW

    @Column(name = "cost", precision = 10, scale = 4)
    private BigDecimal cost;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "dlr_status", length = 50)
    private String dlrStatus; // Delivery Report Status

    @Column(name = "dlr_received_at")
    private LocalDateTime dlrReceivedAt;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

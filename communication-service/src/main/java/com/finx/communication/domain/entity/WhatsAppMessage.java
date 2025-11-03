package com.finx.communication.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking WhatsApp messages
 */
@Entity
@Table(name = "whatsapp_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false, length = 100)
    private String messageId;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile; // Must include country code

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "template_id")
    private Long templateId;

    @Builder.Default
    @Column(name = "language", length = 10)
    private String language = "en";

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Builder.Default
    @Column(name = "message_type", length = 20)
    private String messageType = "TEXT"; // TEXT, IMAGE, VIDEO, DOCUMENT, AUDIO

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "media_filename", length = 255)
    private String mediaFilename;

    @Builder.Default
    @Column(name = "provider", length = 50)
    private String provider = "MSG91";

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "QUEUED"; // QUEUED, SENT, DELIVERED, READ, FAILED

    @Builder.Default
    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM";

    @Column(name = "cost", precision = 10, scale = 4)
    private BigDecimal cost;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.finx.communication.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking Email messages
 */
@Entity
@Table(name = "email_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false, length = 100)
    private String messageId;

    @Column(name = "email_to", nullable = false, length = 100)
    private String toEmail;

    @Column(name = "email_cc", length = 255)
    private String emailCc;

    @Column(name = "email_bcc", length = 255)
    private String emailBcc;

    @Column(name = "from_email", nullable = false, length = 100)
    private String fromEmail;

    @Column(name = "from_name", length = 100)
    private String fromName;

    @Column(name = "reply_to", length = 100)
    private String replyTo;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_code", length = 50)
    private String templateCode;

    @Builder.Default
    @Column(name = "has_attachments")
    private Boolean hasAttachments = false;

    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls;

    @Builder.Default
    @Column(name = "provider", length = 50)
    private String provider = "SENDGRID";

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "QUEUED"; // QUEUED, SENT, DELIVERED, FAILED

    @Builder.Default
    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW

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

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "bounced_at")
    private LocalDateTime bouncedAt;

    @Column(name = "bounce_type", length = 50)
    private String bounceType;

    @Column(name = "bounce_reason", columnDefinition = "TEXT")
    private String bounceReason;

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
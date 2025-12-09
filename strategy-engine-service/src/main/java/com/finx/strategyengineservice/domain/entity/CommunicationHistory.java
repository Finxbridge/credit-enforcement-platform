package com.finx.strategyengineservice.domain.entity;

import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to track all communications sent via strategy execution
 * Includes WhatsApp, Email, SMS, and Notice with document references
 */
@Entity
@Table(name = "communication_history", indexes = {
        @Index(name = "idx_comm_history_case", columnList = "case_id"),
        @Index(name = "idx_comm_history_channel", columnList = "channel"),
        @Index(name = "idx_comm_history_execution", columnList = "execution_id"),
        @Index(name = "idx_comm_history_template", columnList = "template_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "communication_id", unique = true, nullable = false, length = 50)
    private String communicationId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "execution_id")
    private Long executionId;

    @Column(name = "strategy_id")
    private Long strategyId;

    @Column(name = "action_id")
    private Long actionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private CommunicationChannel channel;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_code", length = 50)
    private String templateCode;

    // Recipient Information
    @Column(name = "recipient_mobile", length = 20)
    private String recipientMobile;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Column(name = "recipient_address", columnDefinition = "TEXT")
    private String recipientAddress;

    // Content
    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Document Attachment (DMS Reference)
    @Column(name = "has_document")
    private Boolean hasDocument;

    @Column(name = "dms_document_id", length = 50)
    private String dmsDocumentId;

    @Column(name = "original_document_url", length = 500)
    private String originalDocumentUrl;

    @Column(name = "processed_document_url", length = 500)
    private String processedDocumentUrl;

    @Column(name = "document_type", length = 20)
    private String documentType;

    @Column(name = "document_original_name", length = 255)
    private String documentOriginalName;

    // Status Tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private CommunicationStatus status;

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // Notice-specific fields
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "notice_number", length = 50)
    private String noticeNumber;

    // Timestamps
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CommunicationStatus.PENDING;
        }
        if (hasDocument == null) {
            hasDocument = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

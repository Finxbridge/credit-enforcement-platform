package com.finx.strategyengineservice.domain.dto;

import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for communication history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationHistoryDTO {

    private Long id;
    private String communicationId;
    private Long caseId;
    private Long executionId;
    private Long strategyId;
    private Long actionId;
    private CommunicationChannel channel;
    private Long templateId;
    private String templateCode;

    // Recipient Information
    private String recipientMobile;
    private String recipientEmail;
    private String recipientName;
    private String recipientAddress;

    // Content
    private String subject;
    private String content;

    // Document Attachment (DMS Reference)
    private Boolean hasDocument;
    private String dmsDocumentId;
    private String originalDocumentUrl;
    private String processedDocumentUrl;
    private String documentType;
    private String documentOriginalName;

    // Status Tracking
    private CommunicationStatus status;
    private String providerMessageId;
    private String providerResponse;
    private String failureReason;

    // Notice-specific fields
    private Long noticeId;
    private String noticeNumber;

    // Timestamps
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime failedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating notice via notice-management-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoticeRequest {

    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private String noticeType;  // LEGAL, DEMAND, REMINDER, FINAL_NOTICE, WARNING, SETTLEMENT_OFFER
    private String noticeSubtype;
    private Long templateId;
    private String languageCode;

    // Recipient Information
    private String recipientName;
    private String recipientAddress;
    private String recipientCity;
    private String recipientState;
    private String recipientPincode;

    // DMS Document Reference Fields (from template resolution)
    private String dmsDocumentId;
    private String originalDocumentUrl;
    private String processedDocumentUrl;
    private String documentType;
    private String documentOriginalName;

    // Pre-rendered content (from template resolution)
    private String renderedContent;

    private Long createdBy;
}

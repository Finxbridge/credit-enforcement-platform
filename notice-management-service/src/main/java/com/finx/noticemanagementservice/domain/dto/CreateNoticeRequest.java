package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.NoticeType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoticeRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    private String loanAccountNumber;

    private String customerName;

    @NotNull(message = "Notice type is required")
    private NoticeType noticeType;

    private String noticeSubtype;

    private Long templateId;

    private String languageCode;

    private String recipientName;

    private String recipientAddress;

    private String recipientCity;

    private String recipientState;

    private String recipientPincode;

    // DMS Document Reference Fields
    private String dmsDocumentId;
    private String originalDocumentUrl;
    private String processedDocumentUrl;
    private String documentType;
    private String documentOriginalName;

    // Pre-rendered content (from template resolution)
    private String renderedContent;

    private Long createdBy;
}

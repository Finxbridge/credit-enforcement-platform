package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing notice from notice-management-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {

    private Long id;
    private String noticeNumber;
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private String noticeType;
    private String noticeSubtype;
    private Long templateId;
    private String languageCode;
    private String generatedContent;
    private String pdfUrl;
    private Integer pageCount;

    // DMS Document Reference Fields
    private String dmsDocumentId;
    private String originalDocumentUrl;
    private String processedDocumentUrl;
    private String documentType;
    private String documentOriginalName;

    // Recipient Information
    private String recipientName;
    private String recipientAddress;
    private String recipientCity;
    private String recipientState;
    private String recipientPincode;

    // Status
    private String noticeStatus;
    private LocalDateTime generatedAt;
    private Long generatedBy;
    private Long vendorId;
    private String vendorName;
    private LocalDateTime dispatchedAt;
    private Long dispatchedBy;
    private String trackingNumber;
    private String carrierName;
    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime deliveredAt;
    private Long podId;
    private LocalDateTime rtoAt;
    private String rtoReason;
    private Boolean dispatchSlaBreach;
    private Boolean deliverySlaBreach;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

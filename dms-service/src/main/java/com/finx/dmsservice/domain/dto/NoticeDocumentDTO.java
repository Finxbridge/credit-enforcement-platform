package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import com.finx.dmsservice.domain.enums.StorageProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDocumentDTO {

    private Long id;
    private String noticeNumber;
    private NoticeType noticeType;
    private NoticeStatus noticeStatus;

    // Case/Loan Reference
    private Long caseId;
    private String loanAccountNumber;
    private Long customerId;
    private String customerName;

    // Financial Details
    private BigDecimal principalAmount;
    private BigDecimal totalDues;
    private Integer dpd;
    private String bucket;

    // Template Reference
    private Long templateId;
    private String templateName;

    // Document Storage
    private String documentName;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSizeBytes;
    private StorageProvider storageProvider;
    private String storagePath;

    // Address Information
    private String deliveryAddress;
    private String city;
    private String state;
    private String pincode;
    private String region;

    // Product Information
    private String productType;
    private String productName;

    // Generation Info
    private LocalDateTime generatedAt;
    private Long generatedBy;
    private String generatedByName;

    // Approval Info
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String approvedByName;

    // Dispatch Info
    private LocalDateTime dispatchedAt;
    private Long dispatchVendorId;
    private String dispatchVendorName;
    private String trackingNumber;

    // Delivery Info
    private LocalDateTime deliveredAt;
    private String deliveryProofUrl;
    private String deliveryStatus;

    // Response
    private LocalDate responseDueDate;
    private LocalDateTime responseReceivedAt;
    private String responseNotes;

    // Versioning
    private Integer versionNumber;
    private Long parentNoticeId;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}

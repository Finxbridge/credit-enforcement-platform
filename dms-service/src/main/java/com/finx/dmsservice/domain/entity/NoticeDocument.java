package com.finx.dmsservice.domain.entity;

import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import com.finx.dmsservice.domain.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notice_number", unique = true, nullable = false, length = 100)
    private String noticeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 50)
    private NoticeType noticeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_status", length = 30)
    private NoticeStatus noticeStatus;

    // Case/Loan Reference
    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    // Financial Details
    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "total_dues", precision = 15, scale = 2)
    private BigDecimal totalDues;

    @Column(name = "dpd")
    private Integer dpd;

    @Column(name = "bucket", length = 20)
    private String bucket;

    // Template Reference
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", length = 200)
    private String templateName;

    // Document Storage
    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", length = 50)
    private StorageProvider storageProvider;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "storage_bucket", length = 100)
    private String storageBucket;

    // Address Information
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "region", length = 50)
    private String region;

    // Product Information
    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "product_name", length = 100)
    private String productName;

    // Generation Info
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "generated_by_name", length = 100)
    private String generatedByName;

    // Approval Info
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_by_name", length = 100)
    private String approvedByName;

    // Dispatch Info
    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "dispatch_vendor_id")
    private Long dispatchVendorId;

    @Column(name = "dispatch_vendor_name", length = 100)
    private String dispatchVendorName;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    // Delivery Info
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "delivery_proof_url", length = 500)
    private String deliveryProofUrl;

    @Column(name = "delivery_status", length = 30)
    private String deliveryStatus;

    // Response Deadline
    @Column(name = "response_due_date")
    private LocalDate responseDueDate;

    @Column(name = "response_received_at")
    private LocalDateTime responseReceivedAt;

    @Column(name = "response_notes", columnDefinition = "TEXT")
    private String responseNotes;

    // Metadata
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    // Versioning
    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "parent_notice_id")
    private Long parentNoticeId;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (noticeStatus == null) {
            noticeStatus = NoticeStatus.DRAFT;
        }
        if (versionNumber == null) {
            versionNumber = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.finx.noticemanagementservice.domain.entity;

import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
import com.finx.noticemanagementservice.domain.enums.NoticeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notice_number", unique = true, nullable = false, length = 50)
    private String noticeNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 50)
    private NoticeType noticeType;

    @Column(name = "notice_subtype", length = 50)
    private String noticeSubtype;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "page_count")
    private Integer pageCount;

    // DMS Document Reference Fields (OVH S3 Storage)
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

    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Column(name = "recipient_address", columnDefinition = "TEXT")
    private String recipientAddress;

    @Column(name = "recipient_city", length = 100)
    private String recipientCity;

    @Column(name = "recipient_state", length = 100)
    private String recipientState;

    @Column(name = "recipient_pincode", length = 10)
    private String recipientPincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_status", length = 30)
    private NoticeStatus noticeStatus;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "dispatched_by")
    private Long dispatchedBy;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(name = "expected_delivery_at")
    private LocalDateTime expectedDeliveryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "pod_id")
    private Long podId;

    @Column(name = "rto_at")
    private LocalDateTime rtoAt;

    @Column(name = "rto_reason", length = 255)
    private String rtoReason;

    @Column(name = "dispatch_sla_breach")
    private Boolean dispatchSlaBreach;

    @Column(name = "delivery_sla_breach")
    private Boolean deliverySlaBreach;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
        if (languageCode == null) {
            languageCode = "en";
        }
        if (dispatchSlaBreach == null) {
            dispatchSlaBreach = false;
        }
        if (deliverySlaBreach == null) {
            deliverySlaBreach = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.ReceiptFormat;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import com.finx.collectionsservice.domain.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Receipt Entity
 * Tracks receipt generation for repayments
 */
@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ReceiptStatus status;

    @Column(name = "repayment_id", nullable = false)
    private Long repaymentId;

    @Column(name = "repayment_number", length = 50)
    private String repaymentNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20)
    private ReceiptFormat format;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "pdf_storage_path", length = 500)
    private String pdfStoragePath;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "generated_by_name", length = 100)
    private String generatedByName;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_by_name", length = 100)
    private String verifiedByName;

    @Column(name = "downloaded_at")
    private LocalDateTime downloadedAt;

    @Column(name = "downloaded_by")
    private Long downloadedBy;

    @Column(name = "download_count")
    private Integer downloadCount;

    @Column(name = "emailed_at")
    private LocalDateTime emailedAt;

    @Column(name = "emailed_to", length = 255)
    private String emailedTo;

    @Column(name = "email_count")
    private Integer emailCount;

    @Column(name = "sms_sent_at")
    private LocalDateTime smsSentAt;

    @Column(name = "sms_sent_to", length = 20)
    private String smsSentTo;

    // Cancellation/Void fields
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_by_name", length = 100)
    private String cancelledByName;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "voided_by")
    private Long voidedBy;

    @Column(name = "voided_by_name", length = 100)
    private String voidedByName;

    @Column(name = "void_reason", columnDefinition = "TEXT")
    private String voidReason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        generatedAt = LocalDateTime.now();
        if (downloadCount == null) {
            downloadCount = 0;
        }
        if (emailCount == null) {
            emailCount = 0;
        }
        if (format == null) {
            format = ReceiptFormat.PDF;
        }
        if (status == null) {
            status = ReceiptStatus.GENERATED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

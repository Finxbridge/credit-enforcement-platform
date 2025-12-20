package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.domain.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OTS (One-Time Settlement) Request Entity
 * Tracks settlement requests and their approval workflow
 */
@Entity
@Table(name = "ots_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTSRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ots_number", unique = true, nullable = false, length = 50)
    private String otsNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "original_outstanding", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalOutstanding;

    @Column(name = "proposed_settlement", nullable = false, precision = 15, scale = 2)
    private BigDecimal proposedSettlement;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "waiver_breakdown", columnDefinition = "TEXT")
    private String waiverBreakdown;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 30)
    private PaymentMode paymentMode;

    @Column(name = "installment_count")
    private Integer installmentCount;

    @Column(name = "installment_schedule", columnDefinition = "TEXT")
    private String installmentSchedule;

    @Column(name = "payment_deadline")
    private LocalDate paymentDeadline;

    @Column(name = "intent_captured_at")
    private LocalDateTime intentCapturedAt;

    @Column(name = "intent_captured_by")
    private Long intentCapturedBy;

    @Column(name = "intent_notes", columnDefinition = "TEXT")
    private String intentNotes;

    @Column(name = "borrower_consent")
    private Boolean borrowerConsent;

    @Column(name = "consent_document_url", length = 500)
    private String consentDocumentUrl;

    @Column(name = "request_raised_at")
    private LocalDateTime requestRaisedAt;

    @Column(name = "request_raised_by")
    private Long requestRaisedBy;

    @Column(name = "request_notes", columnDefinition = "TEXT")
    private String requestNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "ots_status", length = 20)
    private OTSStatus otsStatus;

    @Column(name = "current_approval_level")
    private Integer currentApprovalLevel;

    @Column(name = "max_approval_level")
    private Integer maxApprovalLevel;

    @Column(name = "letter_id")
    private Long letterId;

    @Column(name = "letter_generated_at")
    private LocalDateTime letterGeneratedAt;

    @Column(name = "letter_downloaded_at")
    private LocalDateTime letterDownloadedAt;

    @Column(name = "letter_downloaded_by")
    private Long letterDownloadedBy;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "settled_amount", precision = 15, scale = 2)
    private BigDecimal settledAmount;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

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
        if (otsStatus == null) {
            otsStatus = OTSStatus.INTENT_CAPTURED;
        }
        if (currentApprovalLevel == null) {
            currentApprovalLevel = 0;
        }
        if (maxApprovalLevel == null) {
            maxApprovalLevel = 2;
        }
        if (borrowerConsent == null) {
            borrowerConsent = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

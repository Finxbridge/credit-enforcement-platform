package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Repayment Entity
 * Tracks repayments received from customers
 */
@Entity
@Table(name = "repayments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repayment_number", unique = true, length = 50)
    private String repaymentNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "payment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 30)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private RepaymentStatus approvalStatus;

    @Column(name = "current_approval_level")
    private Integer currentApprovalLevel;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "correction_notes", columnDefinition = "TEXT")
    private String correctionNotes;

    @Column(name = "deposit_required_by")
    private LocalDateTime depositRequiredBy;

    @Column(name = "deposited_at")
    private LocalDateTime depositedAt;

    @Column(name = "deposit_sla_status", length = 20)
    private String depositSlaStatus;

    @Column(name = "deposit_sla_breach_hours")
    private Integer depositSlaBreachHours;

    @Column(name = "is_reconciled")
    private Boolean isReconciled;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(name = "reconciled_by")
    private Long reconciledBy;

    @Column(name = "reconciliation_batch_id", length = 100)
    private String reconciliationBatchId;

    @Column(name = "collected_by")
    private Long collectedBy;

    @Column(name = "collection_location", length = 255)
    private String collectionLocation;

    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ots_id")
    private Long otsId;

    @Column(name = "is_ots_payment")
    private Boolean isOtsPayment;

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
        if (approvalStatus == null) {
            approvalStatus = RepaymentStatus.PENDING;
        }
        if (currentApprovalLevel == null) {
            currentApprovalLevel = 1;
        }
        if (isReconciled == null) {
            isReconciled = false;
        }
        if (isOtsPayment == null) {
            isOtsPayment = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

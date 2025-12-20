package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.PTPStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PTP Commitment Entity
 * Tracks Promise to Pay commitments and their fulfillment status
 */
@Entity
@Table(name = "ptp_commitments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PTPCommitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ptp_date", nullable = false)
    private LocalDate ptpDate;

    @Column(name = "ptp_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal ptpAmount;

    @Column(name = "commitment_date", nullable = false)
    private LocalDateTime commitmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ptp_status", nullable = false, length = 20)
    private PTPStatus ptpStatus;

    @Column(name = "payment_received_amount", precision = 15, scale = 2)
    private BigDecimal paymentReceivedAmount;

    @Column(name = "payment_received_date")
    private LocalDate paymentReceivedDate;

    @Column(name = "broken_reason", length = 500)
    private String brokenReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reminder_sent")
    private Boolean reminderSent;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_completed")
    private Boolean followUpCompleted;

    @Column(name = "call_disposition", length = 50)
    private String callDisposition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (ptpStatus == null) {
            ptpStatus = PTPStatus.PENDING;
        }
        if (commitmentDate == null) {
            commitmentDate = LocalDateTime.now();
        }
        if (reminderSent == null) {
            reminderSent = false;
        }
        if (followUpCompleted == null) {
            followUpCompleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

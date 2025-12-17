package com.finx.myworkflow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only PTP Commitment entity for workflow tabs
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

    @Column(name = "ptp_date")
    private LocalDate ptpDate;

    @Column(name = "ptp_amount", precision = 15, scale = 2)
    private BigDecimal ptpAmount;

    @Column(name = "commitment_date")
    private LocalDateTime commitmentDate;

    @Column(name = "ptp_status", length = 20)
    private String ptpStatus;

    @Column(name = "payment_received_amount", precision = 15, scale = 2)
    private BigDecimal paymentReceivedAmount;

    @Column(name = "payment_received_date")
    private LocalDate paymentReceivedDate;

    @Column(name = "broken_reason", length = 500)
    private String brokenReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

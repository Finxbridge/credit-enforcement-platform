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
 * Read-only Repayment entity for workflow tabs
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

    @Column(name = "repayment_number", length = 50)
    private String repaymentNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "payment_amount", precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_mode", length = 30)
    private String paymentMode;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

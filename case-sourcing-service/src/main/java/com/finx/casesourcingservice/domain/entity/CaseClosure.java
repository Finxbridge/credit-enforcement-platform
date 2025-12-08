package com.finx.casesourcingservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to track case closure history
 * Each closure/reopen action creates a new record
 */
@Entity
@Table(name = "case_closure")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    /**
     * Action type: CLOSED or REOPENED
     */
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    /**
     * Previous status before this action (200 or 400)
     */
    @Column(name = "previous_status")
    private Integer previousStatus;

    /**
     * New status after this action (200 or 400)
     */
    @Column(name = "new_status", nullable = false)
    private Integer newStatus;

    /**
     * Reason for closure (required for CLOSED action)
     */
    @Column(name = "closure_reason", length = 255)
    private String closureReason;

    /**
     * Outstanding amount at the time of closure
     */
    @Column(name = "outstanding_amount", precision = 15, scale = 2)
    private BigDecimal outstandingAmount;

    /**
     * DPD at the time of closure
     */
    @Column(name = "dpd")
    private Integer dpd;

    /**
     * Bucket at the time of closure
     */
    @Column(name = "bucket", length = 20)
    private String bucket;

    /**
     * User who performed the action
     */
    @Column(name = "closed_by")
    private Long closedBy;

    /**
     * Username who performed the action
     */
    @Column(name = "closed_by_name", length = 100)
    private String closedByName;

    /**
     * Date when action was performed
     */
    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    /**
     * Additional remarks or notes
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

    /**
     * Reference to batch if bulk closure
     */
    @Column(name = "batch_id", length = 100)
    private String batchId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

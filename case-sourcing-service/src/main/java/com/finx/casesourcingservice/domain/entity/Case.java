package com.finx.casesourcingservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, nullable = false, length = 50)
    private String caseNumber;

    @Column(name = "external_case_id", length = 100)
    private String externalCaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanDetails loan;

    @Column(name = "case_status", length = 20)
    private String caseStatus;

    @Column(name = "case_priority", length = 20)
    private String casePriority;

    @Column(name = "case_opened_at")
    private LocalDateTime caseOpenedAt;

    @Column(name = "case_closed_at")
    private LocalDateTime caseClosedAt;

    @Column(name = "case_closure_reason", length = 100)
    private String caseClosureReason;

    @Column(name = "allocated_to_user_id")
    private Long allocatedToUserId;

    @Column(name = "allocated_to_agency_id")
    private Long allocatedToAgencyId;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @Column(name = "geography_code", length = 50)
    private String geographyCode;

    @Column(name = "city_code", length = 50)
    private String cityCode;

    @Column(name = "state_code", length = 50)
    private String stateCode;

    @Column(name = "ptp_date")
    private LocalDate ptpDate;

    @Column(name = "ptp_amount", precision = 15, scale = 2)
    private BigDecimal ptpAmount;

    @Column(name = "ptp_status", length = 20)
    private String ptpStatus;

    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;

    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "source_file_name", length = 255)
    private String sourceFileName;

    @Column(name = "import_batch_id", length = 100)
    private String importBatchId;

    @Column(name = "collection_cycle", length = 50)
    private String collectionCycle;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}

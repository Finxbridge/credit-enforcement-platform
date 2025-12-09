package com.finx.strategyengineservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only Case entity for strategy execution
 * Maps to cases table in shared database
 * Used for filtering cases based on strategy rules
 * Updated to match unified CSV format
 */
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

    // ==================== CASE IDENTIFICATION ====================

    @Column(name = "case_number", unique = true, nullable = false, length = 50)
    private String caseNumber;

    @Column(name = "external_case_id", length = 100)
    private String externalCaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanDetails loan;

    // ==================== CASE STATUS ====================

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

    // ==================== ALLOCATION ====================

    @Column(name = "allocated_to_user_id")
    private Long allocatedToUserId;

    @Column(name = "primary_agent", length = 100)
    private String primaryAgent; // PRIMARY AGENT

    @Column(name = "secondary_agent", length = 100)
    private String secondaryAgent; // SECONDARY AGENT

    @Column(name = "allocated_to_agency_id")
    private Long allocatedToAgencyId;

    @Column(name = "agency_name", length = 255)
    private String agencyName; // AGENCY NAME

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    // ==================== GEOGRAPHY ====================

    @Column(name = "geography_code", length = 50)
    private String geographyCode;

    @Column(name = "city_code", length = 50)
    private String cityCode;

    @Column(name = "state_code", length = 50)
    private String stateCode;

    @Column(name = "location", length = 100)
    private String location; // LOCATION

    @Column(name = "zone", length = 50)
    private String zone; // ZONE

    // ==================== PTP TRACKING ====================

    @Column(name = "ptp_date")
    private LocalDate ptpDate;

    @Column(name = "ptp_amount", precision = 15, scale = 2)
    private BigDecimal ptpAmount;

    @Column(name = "ptp_status", length = 20)
    private String ptpStatus;

    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;

    // ==================== ASSET DETAILS ====================

    @Column(name = "asset_details", length = 500)
    private String assetDetails; // ASSET DETAILS

    @Column(name = "vehicle_registration_number", length = 50)
    private String vehicleRegistrationNumber; // VEHICLE REGISTRATION NUMBER

    @Column(name = "vehicle_identification_number", length = 50)
    private String vehicleIdentificationNumber; // VEHICLE IDENTIFICATION NUMBER

    @Column(name = "chassis_number", length = 50)
    private String chassisNumber; // CHASSIS NUMBER

    @Column(name = "model_make", length = 100)
    private String modelMake; // MODEL MAKE

    @Column(name = "battery_id", length = 50)
    private String batteryId; // BATTERY ID

    // ==================== DEALER INFORMATION ====================

    @Column(name = "dealer_name", length = 255)
    private String dealerName; // DEALER NAME

    @Column(name = "dealer_address", length = 500)
    private String dealerAddress; // DEALER ADDRESS

    // ==================== FLAGS ====================

    @Column(name = "review_flag", length = 20)
    private String reviewFlag; // REVIEW FLAG

    // ==================== SOURCE TRACKING ====================

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

    /**
     * Numeric status code for case lifecycle
     * 200 = ACTIVE (default for new cases)
     * 400 = CLOSED
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 200;

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}

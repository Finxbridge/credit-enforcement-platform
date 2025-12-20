package com.finx.agencymanagement.domain.entity;

import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Agency Entity
 * Represents a collection agency in the system
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_code", nullable = false, unique = true, length = 50)
    private String agencyCode;

    @Column(name = "agency_name", nullable = false, length = 200)
    private String agencyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "agency_type", nullable = false, length = 50)
    private AgencyType agencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AgencyStatus status = AgencyStatus.PENDING_APPROVAL;

    // Contact Information
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    // Address
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "country", length = 50)
    private String country = "India";

    // KYC Documents
    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Type(JsonType.class)
    @Column(name = "kyc_documents", columnDefinition = "jsonb")
    private String kycDocuments;

    // Bank Details
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIfsc;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    // Contract Details
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "minimum_cases")
    private Integer minimumCases;

    @Column(name = "maximum_cases")
    private Integer maximumCases;

    // Service Areas
    @Type(JsonType.class)
    @Column(name = "service_areas", columnDefinition = "jsonb")
    private String serviceAreas;

    @Type(JsonType.class)
    @Column(name = "service_pincodes", columnDefinition = "jsonb")
    private String servicePincodes;

    // Performance Metrics (cached/denormalized for dashboard)
    @Column(name = "total_cases_allocated")
    private Integer totalCasesAllocated = 0;

    @Column(name = "total_cases_resolved")
    private Integer totalCasesResolved = 0;

    @Column(name = "resolution_rate", precision = 5, scale = 2)
    private BigDecimal resolutionRate = BigDecimal.ZERO;

    @Column(name = "ptp_success_rate", precision = 5, scale = 2)
    private BigDecimal ptpSuccessRate = BigDecimal.ZERO;

    @Column(name = "active_cases_count")
    private Integer activeCasesCount = 0;

    // Approval Workflow
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approval_notes", columnDefinition = "text")
    private String approvalNotes;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

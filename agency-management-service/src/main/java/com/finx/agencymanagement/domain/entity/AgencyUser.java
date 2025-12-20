package com.finx.agencymanagement.domain.entity;

import com.finx.agencymanagement.domain.enums.AgencyUserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Agency User Entity
 * Represents users (agents) belonging to an agency
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Entity
@Table(name = "agency_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "user_code", nullable = false, unique = true, length = 50)
    private String userCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "alternate_mobile", length = 20)
    private String alternateMobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private AgencyUserRole role;

    @Column(name = "designation", length = 100)
    private String designation;

    // Address
    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    // Work Configuration
    @Column(name = "assigned_pincodes", columnDefinition = "jsonb")
    private String assignedPincodes;

    @Column(name = "assigned_geographies", columnDefinition = "jsonb")
    private String assignedGeographies;

    @Column(name = "max_case_capacity")
    private Integer maxCaseCapacity = 50;

    @Column(name = "current_case_count")
    private Integer currentCaseCount = 0;

    // Performance Metrics
    @Column(name = "total_cases_handled")
    private Integer totalCasesHandled = 0;

    @Column(name = "cases_resolved")
    private Integer casesResolved = 0;

    @Column(name = "ptp_captured")
    private Integer ptpCaptured = 0;

    @Column(name = "ptp_kept")
    private Integer ptpKept = 0;

    // Status
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "deactivated_by")
    private Long deactivatedBy;

    @Column(name = "deactivation_reason", columnDefinition = "text")
    private String deactivationReason;

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
}

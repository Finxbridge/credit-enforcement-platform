package com.finx.allocationreallocationservice.domain.entity;

import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity mapping to existing 'allocation_history' table
 * Uses existing database schema from V1_0__tables.sql
 */
@Entity
@Table(name = "allocation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    // New column - will be added via migration
    @Column(name = "external_case_id")
    private String externalCaseId;

    // Maps to existing new_owner_id column
    @Column(name = "new_owner_id", nullable = false)
    private Long allocatedToUserId;

    // Maps to existing new_owner_type column
    @Builder.Default
    @Column(name = "new_owner_type", nullable = false)
    private String newOwnerType = "USER";

    // New column - will be added via migration
    @Column(name = "allocated_to_username")
    private String allocatedToUsername;

    // Maps to existing previous_owner_id column
    @Column(name = "previous_owner_id")
    private Long allocatedFromUserId;

    // Maps to existing previous_owner_type column
    @Builder.Default
    @Column(name = "previous_owner_type")
    private String previousOwnerType = "USER";

    // Maps to existing action_type column
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AllocationAction action;

    @Column(name = "reason")
    private String reason;

    // Maps to existing changed_by column
    @Column(name = "changed_by")
    private Long allocatedBy;

    // Maps to existing changed_at column
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime allocatedAt;

    // New column - will be added via migration
    @Column(name = "batch_id")
    private String batchId;

    // Agency-related fields for tracking agency allocations
    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "agency_code")
    private String agencyCode;

    @Column(name = "agency_name")
    private String agencyName;

    // New column - will be added via migration (though changed_at can serve this
    // purpose)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (allocatedAt == null) {
            allocatedAt = LocalDateTime.now();
        }
    }
}

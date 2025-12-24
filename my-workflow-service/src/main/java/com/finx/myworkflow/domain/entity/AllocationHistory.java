package com.finx.myworkflow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for reading allocation history from the shared allocation_history table.
 * This is a read-only entity for displaying allocation history in the workflow module.
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

    @Column(name = "external_case_id")
    private String externalCaseId;

    @Column(name = "new_owner_id")
    private Long allocatedToUserId;

    @Column(name = "allocated_to_username")
    private String allocatedToUsername;

    @Column(name = "new_owner_type")
    private String newOwnerType;

    @Column(name = "previous_owner_id")
    private Long allocatedFromUserId;

    @Column(name = "previous_owner_type")
    private String previousOwnerType;

    @Column(name = "action_type", nullable = false)
    private String action;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_by")
    private Long allocatedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "batch_id")
    private String batchId;

    // Agency-related fields
    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "agency_code")
    private String agencyCode;

    @Column(name = "agency_name")
    private String agencyName;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}

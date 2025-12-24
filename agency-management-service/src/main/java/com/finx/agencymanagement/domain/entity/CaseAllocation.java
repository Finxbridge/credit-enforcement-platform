package com.finx.agencymanagement.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only entity mapping to 'allocations' table from allocation-reallocation-service.
 * Used to query cases that are allocated in allocation-service but not yet in agency-management.
 */
@Entity
@Table(name = "allocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "external_case_id")
    private String externalCaseId;

    @Column(name = "allocated_to_id")
    private Long allocatedToId;

    @Column(name = "allocated_to_type")
    private String allocatedToType;

    @Column(name = "allocation_status")
    private String allocationStatus;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @Column(name = "allocated_by")
    private Long allocatedBy;
}

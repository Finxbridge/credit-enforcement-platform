package com.finx.agencymanagement.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Agency Case Allocation Entity
 * Tracks case allocations to agencies
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Entity
@Table(name = "agency_case_allocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyCaseAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "external_case_id", length = 100)
    private String externalCaseId;

    /**
     * Agent ID - references users table
     * This is the second level allocation (agency -> agent)
     */
    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "allocation_status", length = 30)
    private String allocationStatus = "ALLOCATED";

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @Column(name = "allocated_by")
    private Long allocatedBy;

    @Column(name = "deallocated_at")
    private LocalDateTime deallocatedAt;

    @Column(name = "deallocated_by")
    private Long deallocatedBy;

    @Column(name = "deallocation_reason", columnDefinition = "text")
    private String deallocationReason;

    @Column(name = "batch_id", length = 100)
    private String batchId;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

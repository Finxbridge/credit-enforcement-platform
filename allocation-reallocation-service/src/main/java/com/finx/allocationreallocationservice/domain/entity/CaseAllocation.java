package com.finx.allocationreallocationservice.domain.entity;

import com.finx.allocationreallocationservice.domain.enums.AllocationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity mapping to existing 'allocations' table
 * Uses existing database schema from V1_0__tables.sql
 */
@Entity
@Table(name = "allocations")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    // New column - will be added via migration
    @Column(name = "external_case_id")
    private String externalCaseId;

    // Maps to existing allocated_to_id column
    @Column(name = "allocated_to_id", nullable = false)
    private Long primaryAgentId;

    // Maps to existing allocated_to_type column
    @Builder.Default
    @Column(name = "allocated_to_type", nullable = false)
    private String allocatedToType = "USER";

    // New column - will be added via migration
    @Column(name = "secondary_agent_id")
    private Long secondaryAgentId;

    // Maps to existing allocation_type column
    @Builder.Default
    @Column(name = "allocation_type")
    private String allocationType = "PRIMARY";

    // Maps to existing workload_percentage column
    @Column(name = "workload_percentage")
    private BigDecimal workloadPercentage;

    // Maps to existing geography_code column
    @Column(name = "geography_code")
    private String geographyCode;

    // Maps to existing allocation_status column
    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false)
    private AllocationStatus status;

    @Column(name = "allocated_by")
    private Long allocatedBy;

    @Column(name = "allocated_at", nullable = false)
    private LocalDateTime allocatedAt;

    // Maps to existing deallocated_at column
    @Column(name = "deallocated_at")
    private LocalDateTime deallocatedAt;

    // New column - will be added via migration
    @Column(name = "allocation_rule_id")
    private Long allocationRuleId;

    // New column - will be added via migration
    @Column(name = "batch_id")
    private String batchId;

    // New columns - will be added via migration
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (allocatedAt == null) {
            allocatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

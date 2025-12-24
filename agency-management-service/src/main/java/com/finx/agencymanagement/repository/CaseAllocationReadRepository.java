package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.CaseAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Read-only repository for accessing 'allocations' table from allocation-reallocation-service.
 * Used to find cases that can be assigned to agency agents.
 */
@Repository
public interface CaseAllocationReadRepository extends JpaRepository<CaseAllocation, Long> {

    /**
     * Find cases that are:
     * 1. Allocated in allocation-reallocation-service (allocations table, status = 'ALLOCATED')
     * 2. NOT already allocated to any agency (not in agency_case_allocations table)
     *
     * These are the cases available for assignment to agency agents.
     */
    @Query(value = """
        SELECT a.* FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        AND a.case_id NOT IN (
            SELECT aca.case_id FROM agency_case_allocations aca
            WHERE aca.allocation_status = 'ALLOCATED'
        )
        ORDER BY a.allocated_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        AND a.case_id NOT IN (
            SELECT aca.case_id FROM agency_case_allocations aca
            WHERE aca.allocation_status = 'ALLOCATED'
        )
        """,
        nativeQuery = true)
    Page<CaseAllocation> findCasesNotAllocatedToAgency(Pageable pageable);

    /**
     * Find cases allocated in allocation-service but not assigned to any agent in agency-management.
     * This includes:
     * 1. Cases in allocations table with status = 'ALLOCATED'
     * 2. Either NOT in agency_case_allocations OR in agency_case_allocations but agent_id is NULL
     */
    @Query(value = """
        SELECT a.* FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        AND (
            a.case_id NOT IN (
                SELECT aca.case_id FROM agency_case_allocations aca
                WHERE aca.allocation_status = 'ALLOCATED' AND aca.agent_id IS NOT NULL
            )
        )
        ORDER BY a.allocated_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        AND (
            a.case_id NOT IN (
                SELECT aca.case_id FROM agency_case_allocations aca
                WHERE aca.allocation_status = 'ALLOCATED' AND aca.agent_id IS NOT NULL
            )
        )
        """,
        nativeQuery = true)
    Page<CaseAllocation> findCasesNotAssignedToAgent(Pageable pageable);

    /**
     * Count cases available for agency assignment
     */
    @Query(value = """
        SELECT COUNT(*) FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        AND a.case_id NOT IN (
            SELECT aca.case_id FROM agency_case_allocations aca
            WHERE aca.allocation_status = 'ALLOCATED' AND aca.agent_id IS NOT NULL
        )
        """, nativeQuery = true)
    Long countCasesNotAssignedToAgent();

    /**
     * Find allocation by case_id
     */
    @Query("SELECT a FROM CaseAllocation a WHERE a.caseId = :caseId AND a.allocationStatus = 'ALLOCATED'")
    java.util.Optional<CaseAllocation> findByCaseIdAndStatus(@org.springframework.data.repository.query.Param("caseId") Long caseId);

    /**
     * Find ALL cases with status = 'ALLOCATED' from allocations table
     * This returns all allocated cases regardless of agency assignment status
     */
    @Query(value = """
        SELECT a.* FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        ORDER BY a.allocated_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM allocations a
        WHERE a.allocation_status = 'ALLOCATED'
        """,
        nativeQuery = true)
    Page<CaseAllocation> findAllAllocatedCases(Pageable pageable);
}

package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.AgencyCaseAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agency Case Allocation Repository
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Repository
public interface AgencyCaseAllocationRepository extends JpaRepository<AgencyCaseAllocation, Long> {

    Optional<AgencyCaseAllocation> findByCaseIdAndAllocationStatus(Long caseId, String allocationStatus);

    List<AgencyCaseAllocation> findByAgencyId(Long agencyId);

    Page<AgencyCaseAllocation> findByAgencyId(Long agencyId, Pageable pageable);

    List<AgencyCaseAllocation> findByAgencyIdAndAllocationStatus(Long agencyId, String allocationStatus);

    Page<AgencyCaseAllocation> findByAgencyIdAndAllocationStatus(Long agencyId, String allocationStatus, Pageable pageable);

    /**
     * Find allocations by agent ID
     */
    List<AgencyCaseAllocation> findByAgentId(Long agentId);

    Page<AgencyCaseAllocation> findByAgentId(Long agentId, Pageable pageable);

    /**
     * Find cases allocated to agency but not yet assigned to any agent
     */
    @Query("SELECT a FROM AgencyCaseAllocation a WHERE a.agencyId = :agencyId AND a.allocationStatus = 'ALLOCATED' AND a.agentId IS NULL")
    Page<AgencyCaseAllocation> findUnassignedCasesByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    /**
     * Find ALL cases allocated to any agency but not yet assigned to any agent
     */
    @Query("SELECT a FROM AgencyCaseAllocation a WHERE a.allocationStatus = 'ALLOCATED' AND a.agentId IS NULL")
    Page<AgencyCaseAllocation> findAllUnassignedCases(Pageable pageable);

    /**
     * Get all case IDs that are already in agency_case_allocations table (allocated to agencies)
     */
    @Query("SELECT a.caseId FROM AgencyCaseAllocation a WHERE a.allocationStatus = 'ALLOCATED'")
    List<Long> findAllAllocatedCaseIds();

    /**
     * Count active allocations by agency
     */
    @Query("SELECT COUNT(a) FROM AgencyCaseAllocation a WHERE a.agencyId = :agencyId AND a.allocationStatus = 'ALLOCATED'")
    Long countActiveAllocationsByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Count active allocations by agent
     */
    @Query("SELECT COUNT(a) FROM AgencyCaseAllocation a WHERE a.agentId = :agentId AND a.allocationStatus = 'ALLOCATED'")
    Long countActiveAllocationsByAgentId(@Param("agentId") Long agentId);

    boolean existsByCaseIdAndAllocationStatus(Long caseId, String allocationStatus);

    List<AgencyCaseAllocation> findByBatchId(String batchId);

    /**
     * Find all case IDs allocated to an agency
     */
    @Query("SELECT a.caseId FROM AgencyCaseAllocation a WHERE a.agencyId = :agencyId AND a.allocationStatus = 'ALLOCATED'")
    List<Long> findCaseIdsByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Update agent assignment for multiple cases
     */
    @Modifying
    @Query("UPDATE AgencyCaseAllocation a SET a.agentId = :agentId WHERE a.caseId IN :caseIds AND a.agencyId = :agencyId AND a.allocationStatus = 'ALLOCATED'")
    int assignCasesToAgent(@Param("agencyId") Long agencyId, @Param("caseIds") List<Long> caseIds, @Param("agentId") Long agentId);

    /**
     * Find all assignments for given case IDs
     * Used to show all agencies/agents a case is assigned to
     */
    @Query("SELECT a FROM AgencyCaseAllocation a WHERE a.caseId IN :caseIds AND a.allocationStatus = 'ALLOCATED'")
    List<AgencyCaseAllocation> findAllocationsByCaseIds(@Param("caseIds") List<Long> caseIds);

    /**
     * Check if a case is already allocated to an agency (can have multiple allocations)
     */
    @Query("SELECT a FROM AgencyCaseAllocation a WHERE a.caseId = :caseId AND a.agencyId = :agencyId AND a.allocationStatus = 'ALLOCATED'")
    Optional<AgencyCaseAllocation> findByCaseIdAndAgencyIdAndStatus(@Param("caseId") Long caseId, @Param("agencyId") Long agencyId);
}

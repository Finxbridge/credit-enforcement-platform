package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for querying cases for strategy execution
 * Provides methods to find allocated cases based on various criteria
 * Implements JpaSpecificationExecutor for dynamic query building
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {

    /**
     * Find case by case number
     */
    Optional<Case> findByCaseNumber(String caseNumber);

    /**
     * Find case by external case ID
     */
    Optional<Case> findByExternalCaseId(String externalCaseId);

    /**
     * Find all allocated cases by user ID
     * Used for strategy execution for specific agent
     */
    @Query("SELECT c FROM Case c WHERE c.allocatedToUserId = :userId AND c.caseStatus = 'ALLOCATED'")
    List<Case> findAllocatedCasesByUser(@Param("userId") Long userId);

    /**
     * Find allocated cases with loan details
     * Eager fetch loan details to avoid N+1 queries
     */
    @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'ALLOCATED'")
    List<Case> findAllAllocatedCasesWithLoan();

    /**
     * Find allocated cases by geography
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'ALLOCATED' AND c.geographyCode IN :geographyCodes")
    List<Case> findAllocatedCasesByGeography(@Param("geographyCodes") List<String> geographyCodes);

    /**
     * Find allocated cases by DPD range
     */
    @Query("SELECT c FROM Case c JOIN c.loan l WHERE c.caseStatus = 'ALLOCATED' " +
           "AND l.dpd BETWEEN :minDpd AND :maxDpd")
    List<Case> findAllocatedCasesByDpdRange(@Param("minDpd") Integer minDpd,
                                             @Param("maxDpd") Integer maxDpd);

    /**
     * Find allocated cases by bucket
     */
    @Query("SELECT c FROM Case c JOIN c.loan l WHERE c.caseStatus = 'ALLOCATED' " +
           "AND l.bucket IN :buckets")
    List<Case> findAllocatedCasesByBucket(@Param("buckets") List<String> buckets);

    /**
     * Find cases with missed PTP (PTP date passed but status not fulfilled)
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'ALLOCATED' " +
           "AND c.ptpDate < :today AND c.ptpStatus != 'FULFILLED'")
    List<Case> findCasesWithMissedPTP(@Param("today") LocalDate today);

    /**
     * Find cases with upcoming PTP (within next N days)
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'ALLOCATED' " +
           "AND c.ptpDate BETWEEN :startDate AND :endDate " +
           "AND c.ptpStatus = 'PENDING'")
    List<Case> findCasesWithUpcomingPTP(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Count allocated cases by user
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.allocatedToUserId = :userId AND c.caseStatus = 'ALLOCATED'")
    Long countAllocatedCasesByUser(@Param("userId") Long userId);

    /**
     * Count allocated cases by case status
     */
    Long countByCaseStatus(String caseStatus);

    /**
     * Find cases by multiple criteria (used with Specifications for dynamic filtering)
     * Already supported through JpaSpecificationExecutor
     */
}

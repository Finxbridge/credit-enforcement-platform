package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.Case;
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
 * Status codes: 200 = ACTIVE, 400 = CLOSED
 * All queries filter by status = 200 (ACTIVE) to exclude closed cases
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {

       /**
        * Find active case by case number
        */
       @Query("SELECT c FROM Case c WHERE c.caseNumber = :caseNumber AND c.status = 200")
       Optional<Case> findByCaseNumberAndActive(@Param("caseNumber") String caseNumber);

       /**
        * Find active case by external case ID
        */
       @Query("SELECT c FROM Case c WHERE c.externalCaseId = :externalCaseId AND c.status = 200")
       Optional<Case> findByExternalCaseIdAndActive(@Param("externalCaseId") String externalCaseId);

       /**
        * Find all active allocated cases by user ID
        * Used for strategy execution for specific agent
        */
       @Query("SELECT c FROM Case c WHERE c.allocatedToUserId = :userId AND c.caseStatus = 'ALLOCATED' AND c.status = 200")
       List<Case> findAllocatedCasesByUser(@Param("userId") Long userId);

       /**
        * Find active allocated cases with loan details
        * Eager fetch loan details to avoid N+1 queries
        */
       @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'ALLOCATED' AND c.status = 200")
       List<Case> findAllAllocatedCasesWithLoan();

       /**
        * Find active allocated cases by geography
        */
       @Query("SELECT c FROM Case c WHERE c.caseStatus = 'ALLOCATED' AND c.geographyCode IN :geographyCodes AND c.status = 200")
       List<Case> findAllocatedCasesByGeography(@Param("geographyCodes") List<String> geographyCodes);

       /**
        * Find active allocated cases by DPD range
        */
       @Query("SELECT c FROM Case c JOIN c.loan l WHERE c.caseStatus = 'ALLOCATED' " +
                     "AND l.dpd BETWEEN :minDpd AND :maxDpd AND c.status = 200")
       List<Case> findAllocatedCasesByDpdRange(@Param("minDpd") Integer minDpd,
                     @Param("maxDpd") Integer maxDpd);

       /**
        * Find active allocated cases by bucket
        */
       @Query("SELECT c FROM Case c JOIN c.loan l WHERE c.caseStatus = 'ALLOCATED' " +
                     "AND l.bucket IN :buckets AND c.status = 200")
       List<Case> findAllocatedCasesByBucket(@Param("buckets") List<String> buckets);

       /**
        * Find active cases with missed PTP (PTP date passed but status not fulfilled)
        */
       @Query("SELECT c FROM Case c WHERE c.caseStatus = 'ALLOCATED' " +
                     "AND c.ptpDate < :today AND c.ptpStatus != 'FULFILLED' AND c.status = 200")
       List<Case> findCasesWithMissedPTP(@Param("today") LocalDate today);

       /**
        * Find active cases with upcoming PTP (within next N days)
        */
       @Query("SELECT c FROM Case c WHERE c.caseStatus = 'ALLOCATED' " +
                     "AND c.ptpDate BETWEEN :startDate AND :endDate " +
                     "AND c.ptpStatus = 'PENDING' AND c.status = 200")
       List<Case> findCasesWithUpcomingPTP(@Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       /**
        * Count active allocated cases by user
        */
       @Query("SELECT COUNT(c) FROM Case c WHERE c.allocatedToUserId = :userId AND c.caseStatus = 'ALLOCATED' AND c.status = 200")
       Long countAllocatedCasesByUser(@Param("userId") Long userId);

       /**
        * Count active allocated cases by case status
        */
       @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = :caseStatus AND c.status = 200")
       Long countByCaseStatusAndActive(@Param("caseStatus") String caseStatus);

       // ============================================
       // LEGACY METHODS (without status filter)
       // Keep for backward compatibility but prefer *AndActive methods
       // ============================================

       Optional<Case> findByCaseNumber(String caseNumber);

       Optional<Case> findByExternalCaseId(String externalCaseId);

       Long countByCaseStatus(String caseStatus);

       /**
        * Find cases by multiple criteria (used with Specifications for dynamic
        * filtering)
        * Already supported through JpaSpecificationExecutor
        */
}

package com.finx.casesourcingservice.repository;

import com.finx.casesourcingservice.domain.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Case Repository
 * Status codes: 200 = ACTIVE, 400 = CLOSED
 * All queries should filter by status = 200 (ACTIVE) unless specifically querying closed cases
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    // ============================================
    // ACTIVE CASES ONLY (status = 200) - DEFAULT
    // ============================================

    /**
     * Find active cases by caseStatus (e.g., UNALLOCATED, ALLOCATED)
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = :caseStatus AND c.status = 200")
    Page<Case> findByCaseStatusAndActive(@Param("caseStatus") String caseStatus, Pageable pageable);

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
     * Count active cases by caseStatus
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = :caseStatus AND c.status = 200")
    Long countByCaseStatusAndActive(@Param("caseStatus") String caseStatus);

    /**
     * Count active cases by import batch ID
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.importBatchId = :importBatchId AND c.status = 200")
    Long countByImportBatchIdAndActive(@Param("importBatchId") String importBatchId);

    /**
     * Count total active cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.status = 200")
    Long countTotalActiveCases();

    // ============================================
    // LEGACY METHODS (without status filter)
    // Keep for backward compatibility but prefer *AndActive methods
    // ============================================

    Page<Case> findByCaseStatus(String caseStatus, Pageable pageable);

    Optional<Case> findByCaseNumber(String caseNumber);

    Optional<Case> findByExternalCaseId(String externalCaseId);

    Long countByCaseStatus(String caseStatus);

    Long countByImportBatchId(String importBatchId);

    @Query("SELECT COUNT(c) FROM Case c")
    Long countTotalCases();

    // ============================================
    // CASE CLOSURE OPERATIONS
    // ============================================

    /**
     * Find case by ID with loan and customer details eagerly loaded
     * Used for case closure to capture all details in closure history
     */
    @Query("SELECT c FROM Case c " +
           "LEFT JOIN FETCH c.loan l " +
           "LEFT JOIN FETCH l.primaryCustomer " +
           "WHERE c.id = :caseId")
    Optional<Case> findByIdWithLoanAndCustomer(@Param("caseId") Long caseId);

    /**
     * Close a case by setting status to 400 and recording closure details
     */
    @Modifying
    @Query("UPDATE Case c SET c.status = 400, c.caseStatus = 'CLOSED', c.caseClosedAt = :closedAt, " +
           "c.caseClosureReason = :reason, c.updatedAt = :closedAt WHERE c.id = :caseId AND c.status = 200")
    int closeCase(@Param("caseId") Long caseId,
                  @Param("reason") String reason,
                  @Param("closedAt") LocalDateTime closedAt);

    /**
     * Bulk close cases
     */
    @Modifying
    @Query("UPDATE Case c SET c.status = 400, c.caseStatus = 'CLOSED', c.caseClosedAt = :closedAt, " +
           "c.caseClosureReason = :reason, c.updatedAt = :closedAt WHERE c.id IN :caseIds AND c.status = 200")
    int closeCases(@Param("caseIds") List<Long> caseIds,
                   @Param("reason") String reason,
                   @Param("closedAt") LocalDateTime closedAt);

    /**
     * Reopen a closed case
     */
    @Modifying
    @Query("UPDATE Case c SET c.status = 200, c.caseStatus = 'ALLOCATED', c.caseClosedAt = NULL, " +
           "c.caseClosureReason = NULL, c.updatedAt = :reopenedAt WHERE c.id = :caseId AND c.status = 400")
    int reopenCase(@Param("caseId") Long caseId, @Param("reopenedAt") LocalDateTime reopenedAt);

    /**
     * Find closed cases
     */
    @Query("SELECT c FROM Case c WHERE c.status = 400")
    Page<Case> findClosedCases(Pageable pageable);

    /**
     * Count closed cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.status = 400")
    Long countClosedCases();

    // ============================================
    // UNALLOCATED REPORT QUERIES (ACTIVE CASES ONLY)
    // ============================================

    @Query("SELECT CAST(c.createdAt AS date) as date, COUNT(c) as count, c.loan.bucket as bucket, c.importBatchId as batchId " +
           "FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.status = 200 AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(c.createdAt AS date), c.loan.bucket, c.importBatchId ORDER BY date")
    List<Object[]> getUnallocatedCasesGroupedByDateBucketSource(@Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.loan.bucket as bucket, COUNT(c) as count " +
           "FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.status = 200 AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.loan.bucket ORDER BY bucket")
    List<Object[]> getUnallocatedCasesByBucket(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.importBatchId as batchId, COUNT(c) as count " +
           "FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.status = 200 AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.importBatchId ORDER BY count DESC")
    List<Object[]> getUnallocatedCasesBySource(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.status = 200 AND c.createdAt BETWEEN :startDate AND :endDate")
    Long countUnallocatedCasesByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}

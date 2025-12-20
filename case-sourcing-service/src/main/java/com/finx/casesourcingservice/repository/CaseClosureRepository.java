package com.finx.casesourcingservice.repository;

import com.finx.casesourcingservice.domain.entity.CaseClosure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for CaseClosure entity
 * Tracks case closure and reopen history
 */
@Repository
public interface CaseClosureRepository extends JpaRepository<CaseClosure, Long> {

    /**
     * Find all closure records for a specific case
     */
    List<CaseClosure> findByCaseIdOrderByClosedAtDesc(Long caseId);

    /**
     * Find all closure records by action type (CLOSED or REOPENED)
     */
    Page<CaseClosure> findByActionOrderByClosedAtDesc(String action, Pageable pageable);

    /**
     * Find closure records by loan ID
     */
    List<CaseClosure> findByLoanIdOrderByClosedAtDesc(Long loanId);

    /**
     * Find closure records by user who performed the action
     */
    Page<CaseClosure> findByClosedByOrderByClosedAtDesc(Long closedBy, Pageable pageable);

    /**
     * Find closure records within a date range
     */
    @Query("SELECT cc FROM CaseClosure cc WHERE cc.closedAt BETWEEN :startDate AND :endDate ORDER BY cc.closedAt DESC")
    Page<CaseClosure> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * Count closures by action type
     */
    Long countByAction(String action);

    /**
     * Count closures within date range
     */
    @Query("SELECT COUNT(cc) FROM CaseClosure cc WHERE cc.action = :action AND cc.closedAt BETWEEN :startDate AND :endDate")
    Long countByActionAndDateRange(@Param("action") String action,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Get latest closure record for a case
     */
    @Query("SELECT cc FROM CaseClosure cc WHERE cc.caseId = :caseId ORDER BY cc.closedAt DESC LIMIT 1")
    CaseClosure findLatestByCaseId(@Param("caseId") Long caseId);

    /**
     * Find closures by closure reason (partial match)
     */
    @Query("SELECT cc FROM CaseClosure cc WHERE LOWER(cc.closureReason) LIKE LOWER(CONCAT('%', :reason, '%')) ORDER BY cc.closedAt DESC")
    Page<CaseClosure> findByClosureReasonContaining(@Param("reason") String reason, Pageable pageable);

    /**
     * Get closure statistics by reason
     */
    @Query("SELECT cc.closureReason, COUNT(cc) FROM CaseClosure cc WHERE cc.action = 'CLOSED' GROUP BY cc.closureReason ORDER BY COUNT(cc) DESC")
    List<Object[]> getClosureStatsByReason();

    /**
     * Find closures by batch ID (for bulk closure tracking)
     */
    List<CaseClosure> findByBatchIdOrderByClosedAtDesc(String batchId);
}

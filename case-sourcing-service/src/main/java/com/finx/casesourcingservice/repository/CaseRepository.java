package com.finx.casesourcingservice.repository;

import com.finx.casesourcingservice.domain.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    Page<Case> findByCaseStatus(String caseStatus, Pageable pageable);

    Optional<Case> findByCaseNumber(String caseNumber);

    Optional<Case> findByExternalCaseId(String externalCaseId);

    Long countByCaseStatus(String caseStatus);

    Long countByImportBatchId(String importBatchId);

    @Query("SELECT COUNT(c) FROM Case c")
    Long countTotalCases();

    // Unallocated Report Queries
    @Query("SELECT CAST(c.createdAt AS date) as date, COUNT(c) as count, c.loan.bucket as bucket, c.importBatchId as batchId " +
           "FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(c.createdAt AS date), c.loan.bucket, c.importBatchId ORDER BY date")
    List<Object[]> getUnallocatedCasesGroupedByDateBucketSource(@Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.loan.bucket as bucket, COUNT(c) as count " +
           "FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.loan.bucket ORDER BY bucket")
    List<Object[]> getUnallocatedCasesByBucket(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.importBatchId as batchId, COUNT(c) as count " +
           "FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.importBatchId ORDER BY count DESC")
    List<Object[]> getUnallocatedCasesBySource(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = 'UNALLOCATED' AND c.createdAt BETWEEN :startDate AND :endDate")
    Long countUnallocatedCasesByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}

package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.AllocationBatch;
import com.finx.allocationreallocationservice.domain.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationBatchRepository extends JpaRepository<AllocationBatch, Long> {

    Optional<AllocationBatch> findByBatchId(String batchId);

    boolean existsByBatchId(String batchId);

    /**
     * Find all batches where batchId starts with the given prefix, ordered by uploadedAt descending
     */
    List<AllocationBatch> findByBatchIdStartingWithOrderByUploadedAtDesc(String prefix);

    @Query("SELECT COUNT(ab) FROM AllocationBatch ab WHERE ab.status = :status")
    Long countByStatus(BatchStatus status);

    @Query("SELECT COALESCE(SUM(ab.totalCases), 0) FROM AllocationBatch ab")
    Long countTotalAllocations();

    @Query("SELECT COALESCE(SUM(ab.successfulAllocations), 0) FROM AllocationBatch ab")
    Long countSuccessfulAllocations();

    @Query("SELECT COALESCE(SUM(ab.failedAllocations), 0) FROM AllocationBatch ab")
    Long countFailedAllocations();

    @Query("SELECT COALESCE(SUM(ab.totalCases), 0) FROM AllocationBatch ab " +
           "WHERE DATE(ab.uploadedAt) = DATE(:date)")
    Long countTotalAllocationsByDate(LocalDateTime date);

    @Query("SELECT COALESCE(SUM(ab.successfulAllocations), 0) FROM AllocationBatch ab " +
           "WHERE DATE(ab.uploadedAt) = DATE(:date)")
    Long countSuccessfulAllocationsByDate(LocalDateTime date);

    @Query("SELECT COALESCE(SUM(ab.failedAllocations), 0) FROM AllocationBatch ab " +
           "WHERE DATE(ab.uploadedAt) = DATE(:date)")
    Long countFailedAllocationsByDate(LocalDateTime date);
}

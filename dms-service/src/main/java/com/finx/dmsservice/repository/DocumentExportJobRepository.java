package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.DocumentExportJob;
import com.finx.dmsservice.domain.enums.ExportFormat;
import com.finx.dmsservice.domain.enums.ExportType;
import com.finx.dmsservice.domain.enums.JobStatus;
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
public interface DocumentExportJobRepository extends JpaRepository<DocumentExportJob, Long> {

    Optional<DocumentExportJob> findByJobId(String jobId);

    Page<DocumentExportJob> findByJobStatus(JobStatus status, Pageable pageable);

    List<DocumentExportJob> findByCreatedBy(Long createdBy);

    @Query("SELECT j FROM DocumentExportJob j WHERE j.jobStatus = 'PENDING' ORDER BY j.createdAt ASC")
    List<DocumentExportJob> findPendingJobs();

    @Query("SELECT j FROM DocumentExportJob j WHERE j.jobStatus = 'COMPLETED' AND j.expiresAt < :now")
    List<DocumentExportJob> findExpiredJobs(@Param("now") LocalDateTime now);

    @Query("SELECT j FROM DocumentExportJob j WHERE j.createdBy = :userId ORDER BY j.createdAt DESC")
    Page<DocumentExportJob> findByUser(@Param("userId") Long userId, Pageable pageable);

    boolean existsByJobId(String jobId);

    // History queries
    @Query("SELECT j FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.jobStatus IN ('COMPLETED', 'FAILED') ORDER BY j.completedAt DESC")
    Page<DocumentExportJob> findHistoryByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT j FROM DocumentExportJob j WHERE j.createdAt BETWEEN :startDate AND :endDate ORDER BY j.createdAt DESC")
    Page<DocumentExportJob> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT j FROM DocumentExportJob j WHERE j.completedAt BETWEEN :startDate AND :endDate ORDER BY j.completedAt DESC")
    Page<DocumentExportJob> findByCompletedDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Count queries by status
    long countByJobStatus(JobStatus status);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.jobStatus = :status")
    long countByUserAndStatus(@Param("userId") Long userId, @Param("status") JobStatus status);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId")
    long countByUser(@Param("userId") Long userId);

    // Count queries by format
    long countByExportFormat(ExportFormat format);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.exportFormat = :format")
    long countByUserAndFormat(@Param("userId") Long userId, @Param("format") ExportFormat format);

    // Count queries by type
    long countByExportType(ExportType type);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.exportType = :type")
    long countByUserAndType(@Param("userId") Long userId, @Param("type") ExportType type);

    // Today's stats
    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdAt >= :startOfDay")
    long countTodayCreated(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.completedAt >= :startOfDay AND j.jobStatus = 'COMPLETED'")
    long countTodayCompleted(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.completedAt >= :startOfDay AND j.jobStatus = 'FAILED'")
    long countTodayFailed(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.createdAt >= :startOfDay")
    long countTodayCreatedByUser(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.completedAt >= :startOfDay AND j.jobStatus = 'COMPLETED'")
    long countTodayCompletedByUser(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.completedAt >= :startOfDay AND j.jobStatus = 'FAILED'")
    long countTodayFailedByUser(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    // Aggregation queries
    @Query("SELECT COALESCE(SUM(j.totalDocuments), 0) FROM DocumentExportJob j WHERE j.jobStatus = 'COMPLETED'")
    long sumTotalDocumentsExported();

    @Query("SELECT COALESCE(SUM(j.failedDocuments), 0) FROM DocumentExportJob j")
    long sumTotalDocumentsFailed();

    @Query("SELECT COALESCE(SUM(j.exportFileSizeBytes), 0) FROM DocumentExportJob j WHERE j.jobStatus = 'COMPLETED'")
    long sumTotalExportSize();

    @Query("SELECT COALESCE(SUM(j.totalDocuments), 0) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.jobStatus = 'COMPLETED'")
    long sumTotalDocumentsExportedByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(j.failedDocuments), 0) FROM DocumentExportJob j WHERE j.createdBy = :userId")
    long sumTotalDocumentsFailedByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(j.exportFileSizeBytes), 0) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.jobStatus = 'COMPLETED'")
    long sumTotalExportSizeByUser(@Param("userId") Long userId);

    // Expired files count
    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.jobStatus = 'COMPLETED' AND j.expiresAt < :now")
    long countExpiredFiles(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(j) FROM DocumentExportJob j WHERE j.createdBy = :userId AND j.jobStatus = 'COMPLETED' AND j.expiresAt < :now")
    long countExpiredFilesByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Failed jobs that can be retried
    @Query("SELECT j FROM DocumentExportJob j WHERE j.jobStatus = 'FAILED' AND j.createdBy = :userId ORDER BY j.completedAt DESC")
    List<DocumentExportJob> findFailedJobsByUser(@Param("userId") Long userId);
}

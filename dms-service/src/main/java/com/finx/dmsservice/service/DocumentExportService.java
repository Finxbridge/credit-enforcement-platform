package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.*;
import com.finx.dmsservice.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentExportService {

    // Job Creation
    DocumentExportJobDTO createExportJob(CreateExportJobRequest request, Long requestedBy);

    DocumentExportJobDTO createSingleExport(SingleExportRequest request, Long userId);

    DocumentExportJobDTO createBulkExport(BulkExportRequest request, Long userId);

    // Job Retrieval
    DocumentExportJobDTO getExportJobById(Long id);

    DocumentExportJobDTO getExportJobByJobId(String jobId);

    List<DocumentExportJobDTO> getExportJobsByUser(Long userId);

    Page<DocumentExportJobDTO> getAllExportJobs(Pageable pageable);

    Page<DocumentExportJobDTO> getExportJobsByStatus(JobStatus status, Pageable pageable);

    // Job Processing
    DocumentExportJobDTO processExportJob(Long id);

    ExportProgressDTO getExportProgress(String jobId);

    void cancelExportJob(Long id);

    void deleteExportJob(Long id);

    // Download
    byte[] downloadExportFile(Long id);

    byte[] downloadExportFile(Long id, Long userId);

    String getDownloadUrl(Long id, Long userId);

    // History
    Page<ExportHistoryDTO> getExportHistory(Long userId, Pageable pageable);

    Page<ExportHistoryDTO> getExportHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Summary
    ExportSummaryDTO getExportSummary();

    ExportSummaryDTO getExportSummaryByUser(Long userId);

    // Cleanup
    void cleanupExpiredJobs();

    // Retry
    DocumentExportJobDTO retryFailedJob(Long id, Long userId);
}

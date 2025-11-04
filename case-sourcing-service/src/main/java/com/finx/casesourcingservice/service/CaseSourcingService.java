package com.finx.casesourcingservice.service;

import com.finx.casesourcingservice.domain.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for case sourcing operations
 */
public interface CaseSourcingService {

    /**
     * Get dashboard summary
     */
    DashboardSummaryDTO getDashboardSummary();

    /**
     * Get source-wise statistics
     */
    List<SourceStatsDTO> getSourceStats();

    /**
     * Get recent uploads
     */
    List<RecentUploadDTO> getRecentUploads(Pageable pageable);

    /**
     * Upload case data via CSV
     */
    BatchUploadResponseDTO uploadCases(MultipartFile file, String uploadedBy);

    /**
     * Get upload batch status
     */
    BatchStatusDTO getBatchStatus(String batchId);

    /**
     * Get batch summary
     */
    BatchSummaryDTO getBatchSummary(String batchId);

    /**
     * Get failed case records
     */
    List<FailedCaseRecordDTO> getFailedCaseRecords(String batchId);

    /**
     * Get unallocated cases
     */
    Page<UnallocatedCaseDTO> getUnallocatedCases(Pageable pageable);

    /**
     * Get unallocated case details
     */
    UnallocatedCaseDTO getUnallocatedCaseDetails(Long caseId);

    /**
     * Re-upload corrected CSV
     */
    BatchUploadResponseDTO reuploadCases(String batchId, MultipartFile file, String uploadedBy);

    /**
     * Export failed cases to CSV
     */
    byte[] exportFailedCases(String batchId);

    /**
     * Export batch cases to CSV
     */
    byte[] exportBatchCases(String batchId);

    /**
     * Get intake report for a date range
     */
    IntakeReportDTO getIntakeReport(String startDate, String endDate);

    /**
     * Get unallocated cases report for a date range
     */
    UnallocatedReportDTO getUnallocatedCasesReport(String startDate, String endDate);
}

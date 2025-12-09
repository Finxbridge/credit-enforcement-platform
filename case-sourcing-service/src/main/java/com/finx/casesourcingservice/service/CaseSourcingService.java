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

    /**
     * FR-CS-5: Advanced case search with multiple filters
     *
     * @param request  Search criteria
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    Page<CaseSearchResultDTO> searchCases(CaseSearchRequest request, Pageable pageable);

    /**
     * FR-WF-2: Get complete case activity timeline
     *
     * @param caseId Case ID
     * @return Timeline with all events (calls, PTPs, payments, notes, communications)
     */
    CaseTimelineDTO getCaseTimeline(Long caseId);

    /**
     * Get all batches with optional status filter and pagination
     *
     * @param status   Optional batch status filter (PROCESSING, COMPLETED, FAILED, PARTIAL)
     * @param pageable Pagination parameters
     * @return Paginated list of batches
     */
    Page<RecentUploadDTO> getAllBatches(String status, Pageable pageable);

    /**
     * Evict unallocated cases cache.
     * Called by allocation-reallocation service after case allocation to ensure fresh data.
     */
    void evictUnallocatedCasesCache();

    // NOTE: Case Closure operations moved to Collections Service (CycleClosureService)
}

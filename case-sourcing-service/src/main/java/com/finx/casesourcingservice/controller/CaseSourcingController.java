package com.finx.casesourcingservice.controller;

import com.finx.casesourcingservice.domain.dto.*;
import com.finx.casesourcingservice.service.CaseSourcingService;
import com.finx.casesourcingservice.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for Case Sourcing operations
 * All endpoints follow the CommonResponse pattern
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/case")
@RequiredArgsConstructor
public class CaseSourcingController {

    private final CaseSourcingService caseSourcingService;

    /**
     * 3.1.1 Get Dashboard Summary
     * GET /api/v1/case/source/summary
     */
    @GetMapping("/source/summary")
    public ResponseEntity<CommonResponse<DashboardSummaryDTO>> getDashboardSummary() {
        log.info("GET /case/source/summary - Fetching dashboard summary");
        DashboardSummaryDTO summary = caseSourcingService.getDashboardSummary();
        return ResponseWrapper.ok("Case sourcing summary retrieved successfully.", summary);
    }

    /**
     * 3.1.2 Fetch Data Source-wise Intake Metrics
     * GET /api/v1/case/source/stats
     */
    @GetMapping("/source/stats")
    public ResponseEntity<CommonResponse<List<SourceStatsDTO>>> getSourceStats() {
        log.info("GET /case/source/stats - Fetching source-wise statistics");
        List<SourceStatsDTO> stats = caseSourcingService.getSourceStats();
        return ResponseWrapper.ok("Case sourcing statistics retrieved successfully.", stats);
    }

    /**
     * 3.1.3 List Recent Uploads
     * GET /api/v1/case/source/recent-uploads
     */
    @GetMapping("/source/recent-uploads")
    public ResponseEntity<CommonResponse<List<RecentUploadDTO>>> getRecentUploads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /case/source/recent-uploads - Fetching recent uploads (page: {}, size: {})", page, size);
        Pageable pageable = PageRequest.of(page, size);
        List<RecentUploadDTO> uploads = caseSourcingService.getRecentUploads(pageable);
        return ResponseWrapper.ok("Recent uploads retrieved successfully.", uploads);
    }

    /**
     * 3.1.4 Upload Case Data via CSV
     * POST /api/v1/case/source/upload
     */
    @PostMapping("/source/upload")
    public ResponseEntity<CommonResponse<BatchUploadResponseDTO>> uploadCases(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false, defaultValue = "system") String uploadedBy) {
        log.info("POST /case/source/upload - Uploading case data from file: {}", file.getOriginalFilename());
        BatchUploadResponseDTO response = caseSourcingService.uploadCases(file, uploadedBy);
        return ResponseWrapper.accepted("Case sourcing initiated. Validation in progress.", response);
    }

    /**
     * 3.1.5 Get Upload Batch Status
     * GET /api/v1/case/source/{batchId}/status
     */
    @GetMapping("/source/{batchId}/status")
    public ResponseEntity<CommonResponse<BatchStatusDTO>> getBatchStatus(
            @PathVariable String batchId) {
        log.info("GET /case/source/{}/status - Fetching batch status", batchId);
        BatchStatusDTO status = caseSourcingService.getBatchStatus(batchId);
        return ResponseWrapper.ok("Case sourcing status retrieved successfully.", status);
    }

    /**
     * 3.1.6 Get Batch Summary
     * GET /api/v1/case/source/{batchId}/summary
     */
    @GetMapping("/source/{batchId}/summary")
    public ResponseEntity<CommonResponse<BatchSummaryDTO>> getBatchSummary(
            @PathVariable String batchId) {
        log.info("GET /case/source/{}/summary - Fetching batch summary", batchId);
        BatchSummaryDTO summary = caseSourcingService.getBatchSummary(batchId);
        return ResponseWrapper.ok("Batch summary retrieved successfully.", summary);
    }

    /**
     * 3.1.7 List Failed Case Records
     * GET /api/v1/case/source/{batchId}/errors
     */
    @GetMapping("/source/{batchId}/errors")
    public ResponseEntity<CommonResponse<List<FailedCaseRecordDTO>>> getFailedCaseRecords(
            @PathVariable String batchId) {
        log.info("GET /case/source/{}/errors - Fetching failed case records", batchId);
        List<FailedCaseRecordDTO> errors = caseSourcingService.getFailedCaseRecords(batchId);
        return ResponseWrapper.ok("Failed case records retrieved successfully.", errors);
    }

    /**
     * 3.1.8 Download Failed Cases
     * GET /api/v1/case/source/{batchId}/errors/export
     * Returns CSV file
     */
    @GetMapping("/source/{batchId}/errors/export")
    public ResponseEntity<byte[]> exportFailedCases(@PathVariable String batchId) {
        log.info("GET /case/source/{}/errors/export - Exporting failed cases", batchId);

        byte[] csvData = caseSourcingService.exportFailedCases(batchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "failed_cases_" + batchId + ".csv");
        headers.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    /**
     * 3.1.9 Get Unallocated Cases
     * GET /api/v1/case/unallocated
     */
    @GetMapping("/unallocated")
    public ResponseEntity<CommonResponse<Page<UnallocatedCaseDTO>>> getUnallocatedCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /case/unallocated - Fetching unallocated cases (page: {}, size: {})", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UnallocatedCaseDTO> cases = caseSourcingService.getUnallocatedCases(pageable);
        return ResponseWrapper.ok("Unallocated cases retrieved successfully.", cases);
    }

    /**
     * 3.1.10 View Unallocated Case Details
     * GET /api/v1/case/unallocated/{caseId}
     */
    @GetMapping("/unallocated/{caseId}")
    public ResponseEntity<CommonResponse<UnallocatedCaseDTO>> getUnallocatedCaseDetails(
            @PathVariable Long caseId) {
        log.info("GET /case/unallocated/{} - Fetching unallocated case details", caseId);
        UnallocatedCaseDTO caseDetails = caseSourcingService.getUnallocatedCaseDetails(caseId);
        return ResponseWrapper.ok("Unallocated case details retrieved successfully.", caseDetails);
    }

    /**
     * 3.1.11 Export Cases
     * GET /api/v1/case/source/{batchId}/export
     * Returns CSV file
     */
    @GetMapping("/source/{batchId}/export")
    public ResponseEntity<byte[]> exportCases(@PathVariable String batchId) {
        log.info("GET /case/source/{}/export - Exporting cases", batchId);

        byte[] csvData = caseSourcingService.exportBatchCases(batchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "cases_" + batchId + ".csv");
        headers.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    /**
     * 3.1.12 Re-upload Corrected CSV
     * POST /api/v1/case/source/{batchId}/reupload
     */
    @PostMapping("/source/{batchId}/reupload")
    public ResponseEntity<CommonResponse<BatchUploadResponseDTO>> reuploadCases(
            @PathVariable String batchId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false, defaultValue = "system") String uploadedBy) {
        log.info("POST /case/source/{}/reupload - Re-uploading corrected cases", batchId);
        BatchUploadResponseDTO response = caseSourcingService.reuploadCases(batchId, file, uploadedBy);
        return ResponseWrapper.accepted("Case re-upload initiated. Validation in progress.", response);
    }

    /**
     * 3.1.13 Get Intake Report
     * GET /api/v1/case/reports/intake
     * Returns date-wise intake metrics, success rates, and source-wise breakdown
     */
    @GetMapping("/reports/intake")
    public ResponseEntity<CommonResponse<IntakeReportDTO>> getIntakeReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("GET /case/reports/intake - Fetching intake report (startDate: {}, endDate: {})",
                startDate, endDate);

        IntakeReportDTO report = caseSourcingService.getIntakeReport(startDate, endDate);
        return ResponseWrapper.ok("Intake report generated successfully.", report);
    }

    /**
     * 3.1.14 Get Unallocated Cases Report
     * GET /api/v1/case/reports/unallocated
     * Returns unallocated cases grouped by date, source, bucket with detailed breakdown
     */
    @GetMapping("/reports/unallocated")
    public ResponseEntity<CommonResponse<UnallocatedReportDTO>> getUnallocatedCasesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("GET /case/reports/unallocated - Fetching unallocated cases report (startDate: {}, endDate: {})",
                startDate, endDate);

        UnallocatedReportDTO report = caseSourcingService.getUnallocatedCasesReport(startDate, endDate);
        return ResponseWrapper.ok("Unallocated cases report generated successfully.", report);
    }
}

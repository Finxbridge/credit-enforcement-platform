package com.finx.casesourcingservice.controller;

import com.finx.casesourcingservice.domain.dto.*;
import com.finx.casesourcingservice.service.CaseSourcingService;
import com.finx.casesourcingservice.service.PTPService;
import com.finx.casesourcingservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.time.LocalDate;
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
@Tag(name = "Case Sourcing & PTP Management", description = "APIs for case intake, validation, and Promise to Pay management")
public class CaseSourcingController {

    private final CaseSourcingService caseSourcingService;
    private final PTPService ptpService;
    private final com.finx.casesourcingservice.util.csv.CsvTemplateGenerator csvTemplateGenerator;
    private final com.finx.casesourcingservice.util.csv.CsvHeaderValidator csvHeaderValidator;

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
     * 3.1.3.1 Validate CSV Headers Before Upload
     * POST /api/v1/case/source/validate-headers
     */
    @PostMapping("/source/validate-headers")
    @Operation(summary = "Validate CSV headers before upload",
               description = "Validate CSV headers to detect missing/unknown headers and get suggestions for typos")
    public ResponseEntity<CommonResponse<HeaderValidationResult>> validateCaseUploadHeaders(
            @RequestPart("file") MultipartFile file) {
        log.info("POST /case/source/validate-headers - Validating headers for file: {}", file.getOriginalFilename());

        HeaderValidationResult validation = csvHeaderValidator.validateCaseUploadHeaders(file);

        if (validation.getIsValid()) {
            return ResponseWrapper.ok("CSV headers are valid.", validation);
        } else {
            return ResponseWrapper.ok("CSV headers validation failed.", validation);
        }
    }

    /**
     * 3.1.3.2 Download CSV Template
     * GET /api/v1/case/source/upload/template
     */
    @GetMapping("/source/upload/template")
    @Operation(summary = "Download case upload CSV template",
               description = "Download CSV template with headers and optional sample data")
    public ResponseEntity<byte[]> downloadCaseUploadTemplate(
            @RequestParam(defaultValue = "false") boolean includeSample) {
        log.info("GET /case/source/upload/template - Downloading template (includeSample: {})", includeSample);

        byte[] csvData = csvTemplateGenerator.generateCaseUploadTemplate(includeSample);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "case_upload_template.csv");
        headers.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
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

    // ===================================================
    // CASE SEARCH & TIMELINE ENDPOINTS
    // ===================================================

    /**
     * FR-CS-5: Advanced case search with filters
     * GET /api/v1/case/search
     */
    @GetMapping("/search")
    @Operation(summary = "Advanced case search", description = "Search cases with multiple filters")
    public ResponseEntity<CommonResponse<Page<CaseSearchResultDTO>>> searchCases(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String loanAccountNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String caseStatus,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) Integer minDpd,
            @RequestParam(required = false) Integer maxDpd,
            @RequestParam(required = false) String geographyCode,
            @RequestParam(required = false) Long allocatedToUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /case/search - Searching cases with filters");

        CaseSearchRequest searchRequest = CaseSearchRequest.builder()
                .caseNumber(caseNumber)
                .loanAccountNumber(loanAccountNumber)
                .customerName(customerName)
                .mobileNumber(mobileNumber)
                .caseStatus(caseStatus)
                .bucket(bucket)
                .minDpd(minDpd)
                .maxDpd(maxDpd)
                .geographyCode(geographyCode)
                .allocatedToUserId(allocatedToUserId)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<CaseSearchResultDTO> results = caseSourcingService.searchCases(searchRequest, pageable);

        return ResponseWrapper.ok("Cases retrieved successfully.", results);
    }

    /**
     * FR-WF-2: Get case timeline (complete activity history)
     * GET /api/v1/case/{caseId}/timeline
     */
    @GetMapping("/{caseId}/timeline")
    @Operation(summary = "Get case timeline",
               description = "Get complete activity timeline including calls, PTPs, payments, notes, and communications")
    public ResponseEntity<CommonResponse<CaseTimelineDTO>> getCaseTimeline(@PathVariable Long caseId) {
        log.info("GET /case/{}/timeline - Fetching case timeline", caseId);

        CaseTimelineDTO timeline = caseSourcingService.getCaseTimeline(caseId);

        return ResponseWrapper.ok("Case timeline retrieved successfully.", timeline);
    }

    // ===================================================
    // PTP (Promise to Pay) Management Endpoints
    // ===================================================

    /**
     * FR-PTP-1: Capture PTP commitment
     * POST /api/v1/case/ptp
     */
    @PostMapping("/ptp")
    @Operation(summary = "Capture PTP commitment", description = "Record a Promise to Pay commitment from borrower")
    public ResponseEntity<CommonResponse<PTPResponse>> capturePTP(
            @Valid @RequestBody CapturePTPRequest request) {
        log.info("POST /case/ptp - Capturing PTP for case: {}", request.getCaseId());
        PTPResponse response = ptpService.capturePTP(request);
        return ResponseWrapper.ok("PTP captured successfully.", response);
    }

    /**
     * FR-PTP-2: Get PTPs due today or specific date
     * GET /api/v1/case/ptp/due
     */
    @GetMapping("/ptp/due")
    @Operation(summary = "Get PTPs due", description = "Get all PTPs due on a specific date (defaults to today)")
    public ResponseEntity<CommonResponse<List<PTPCaseDTO>>> getPTPsDue(
            @RequestParam(required = false) LocalDate dueDate,
            @RequestParam(required = false) Long userId) {
        LocalDate targetDate = dueDate != null ? dueDate : LocalDate.now();
        log.info("GET /case/ptp/due - Fetching PTPs due on: {} for user: {}", targetDate, userId);
        List<PTPCaseDTO> ptps = ptpService.getPTPsDue(targetDate, userId);
        return ResponseWrapper.ok("PTPs due retrieved successfully.", ptps);
    }

    /**
     * FR-PTP-3: Get broken PTPs
     * GET /api/v1/case/ptp/broken
     */
    @GetMapping("/ptp/broken")
    @Operation(summary = "Get broken PTPs", description = "Get all PTPs that are past due date (broken commitments)")
    public ResponseEntity<CommonResponse<List<PTPCaseDTO>>> getBrokenPTPs(
            @RequestParam(required = false) Long userId) {
        log.info("GET /case/ptp/broken - Fetching broken PTPs for user: {}", userId);
        List<PTPCaseDTO> ptps = ptpService.getBrokenPTPs(userId);
        return ResponseWrapper.ok("Broken PTPs retrieved successfully.", ptps);
    }

    /**
     * Get PTP by ID
     * GET /api/v1/case/ptp/{ptpId}
     */
    @GetMapping("/ptp/{ptpId}")
    @Operation(summary = "Get PTP details", description = "Get details of a specific PTP commitment")
    public ResponseEntity<CommonResponse<PTPResponse>> getPTPById(@PathVariable Long ptpId) {
        log.info("GET /case/ptp/{} - Fetching PTP details", ptpId);
        PTPResponse response = ptpService.getPTPById(ptpId);
        return ResponseWrapper.ok("PTP details retrieved successfully.", response);
    }

    /**
     * Get all PTPs for a case
     * GET /api/v1/case/{caseId}/ptp
     */
    @GetMapping("/{caseId}/ptp")
    @Operation(summary = "Get case PTPs", description = "Get all PTP commitments for a specific case")
    public ResponseEntity<CommonResponse<List<PTPResponse>>> getCasePTPs(@PathVariable Long caseId) {
        log.info("GET /case/{}/ptp - Fetching PTPs for case", caseId);
        List<PTPResponse> ptps = ptpService.getPTPsByCase(caseId);
        return ResponseWrapper.ok("Case PTPs retrieved successfully.", ptps);
    }

    /**
     * Update PTP status
     * PUT /api/v1/case/ptp/{ptpId}
     */
    @PutMapping("/ptp/{ptpId}")
    @Operation(summary = "Update PTP status", description = "Mark PTP as KEPT, BROKEN, RENEWED, etc.")
    public ResponseEntity<CommonResponse<PTPResponse>> updatePTPStatus(
            @PathVariable Long ptpId,
            @Valid @RequestBody UpdatePTPRequest request) {
        log.info("PUT /case/ptp/{} - Updating PTP status to: {}", ptpId, request.getPtpStatus());
        PTPResponse response = ptpService.updatePTPStatus(ptpId, request);
        return ResponseWrapper.ok("PTP status updated successfully.", response);
    }

    /**
     * Get PTP statistics
     * GET /api/v1/case/ptp/stats
     */
    @GetMapping("/ptp/stats")
    @Operation(summary = "Get PTP statistics", description = "Get PTP performance metrics and statistics")
    public ResponseEntity<CommonResponse<PTPStatsDTO>> getPTPStats(
            @RequestParam(required = false) Long userId) {
        log.info("GET /case/ptp/stats - Fetching PTP statistics for user: {}", userId);
        PTPStatsDTO stats = ptpService.getPTPStats(userId);
        return ResponseWrapper.ok("PTP statistics retrieved successfully.", stats);
    }

    /**
     * Get PTPs by status (paginated)
     * GET /api/v1/case/ptp/status/{status}
     */
    @GetMapping("/ptp/status/{status}")
    @Operation(summary = "Get PTPs by status", description = "Get PTPs filtered by status with pagination")
    public ResponseEntity<CommonResponse<Page<PTPResponse>>> getPTPsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /case/ptp/status/{} - Fetching PTPs (page: {}, size: {})", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PTPResponse> ptps = ptpService.getPTPsByStatus(status, pageable);
        return ResponseWrapper.ok("PTPs retrieved successfully.", ptps);
    }
}

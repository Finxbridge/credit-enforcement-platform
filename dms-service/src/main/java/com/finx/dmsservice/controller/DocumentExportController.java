package com.finx.dmsservice.controller;

import com.finx.dmsservice.domain.dto.*;
import com.finx.dmsservice.domain.enums.JobStatus;
import com.finx.dmsservice.service.DocumentExportService;
import com.finx.dmsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dms/export")
@RequiredArgsConstructor
@Tag(name = "Document Export", description = "APIs for document export and download management")
public class DocumentExportController {

    private final DocumentExportService exportService;

    // ==================== JOB CREATION ====================

    @PostMapping
    @Operation(summary = "Create export job", description = "Create a new export job with custom criteria")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> createExportJob(
            @Valid @RequestBody CreateExportJobRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /dms/export - Creating export job for user: {}", userId);
        DocumentExportJobDTO response = exportService.createExportJob(request, userId);
        return ResponseWrapper.created("Export job created successfully", response);
    }

    @PostMapping("/single")
    @Operation(summary = "Export single document", description = "Export a single document with format selection")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> createSingleExport(
            @Valid @RequestBody SingleExportRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /dms/export/single - Creating single export for document: {}", request.getDocumentId());
        DocumentExportJobDTO response = exportService.createSingleExport(request, userId);
        return ResponseWrapper.created("Single export job created", response);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk export documents", description = "Export multiple documents with ZIP packaging")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> createBulkExport(
            @Valid @RequestBody BulkExportRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /dms/export/bulk - Creating bulk export for {} documents",
                request.getDocumentIds() != null ? request.getDocumentIds().size() : 0);
        DocumentExportJobDTO response = exportService.createBulkExport(request, userId);
        return ResponseWrapper.created("Bulk export job created", response);
    }

    // ==================== JOB RETRIEVAL ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get export job by ID", description = "Get export job details by ID")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> getExportJobById(@PathVariable Long id) {
        log.info("GET /dms/export/{} - Fetching export job", id);
        DocumentExportJobDTO response = exportService.getExportJobById(id);
        return ResponseWrapper.ok("Export job retrieved successfully", response);
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get export job by job ID", description = "Get export job details by job ID string")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> getExportJobByJobId(@PathVariable String jobId) {
        log.info("GET /dms/export/job/{} - Fetching export job", jobId);
        DocumentExportJobDTO response = exportService.getExportJobByJobId(jobId);
        return ResponseWrapper.ok("Export job retrieved successfully", response);
    }

    @GetMapping("/my-jobs")
    @Operation(summary = "Get my export jobs", description = "Get export jobs created by current user")
    public ResponseEntity<CommonResponse<List<DocumentExportJobDTO>>> getMyExportJobs(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/export/my-jobs - Fetching export jobs for user: {}", userId);
        List<DocumentExportJobDTO> jobs = exportService.getExportJobsByUser(userId);
        return ResponseWrapper.ok("Export jobs retrieved successfully", jobs);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get export jobs by user", description = "Get export jobs for a specific user")
    public ResponseEntity<CommonResponse<List<DocumentExportJobDTO>>> getExportJobsByUser(@PathVariable Long userId) {
        log.info("GET /dms/export/user/{} - Fetching export jobs", userId);
        List<DocumentExportJobDTO> jobs = exportService.getExportJobsByUser(userId);
        return ResponseWrapper.ok("Export jobs retrieved successfully", jobs);
    }

    @GetMapping
    @Operation(summary = "Get all export jobs", description = "Get all export jobs with pagination")
    public ResponseEntity<CommonResponse<Page<DocumentExportJobDTO>>> getAllExportJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/export - Fetching all export jobs");
        Page<DocumentExportJobDTO> jobs = exportService.getAllExportJobs(pageable);
        return ResponseWrapper.ok("Export jobs retrieved successfully", jobs);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get export jobs by status", description = "Get export jobs filtered by status")
    public ResponseEntity<CommonResponse<Page<DocumentExportJobDTO>>> getExportJobsByStatus(
            @PathVariable JobStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/export/status/{} - Fetching export jobs", status);
        Page<DocumentExportJobDTO> jobs = exportService.getExportJobsByStatus(status, pageable);
        return ResponseWrapper.ok("Export jobs retrieved successfully", jobs);
    }

    // ==================== JOB PROCESSING ====================

    @PostMapping("/{id}/process")
    @Operation(summary = "Process export job", description = "Start processing an export job")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> processExportJob(@PathVariable Long id) {
        log.info("POST /dms/export/{}/process - Processing export job", id);
        DocumentExportJobDTO response = exportService.processExportJob(id);
        return ResponseWrapper.ok("Export job processing started", response);
    }

    @GetMapping("/jobs/{jobId}/status")
    @Operation(summary = "Get export progress", description = "Get real-time export progress")
    public ResponseEntity<CommonResponse<ExportProgressDTO>> getExportProgress(@PathVariable String jobId) {
        log.info("GET /dms/export/jobs/{}/status - Fetching export progress", jobId);
        ExportProgressDTO progress = exportService.getExportProgress(jobId);
        return ResponseWrapper.ok("Export progress retrieved", progress);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel export job", description = "Cancel a pending or processing export job")
    public ResponseEntity<CommonResponse<Void>> cancelExportJob(@PathVariable Long id) {
        log.info("POST /dms/export/{}/cancel - Cancelling export job", id);
        exportService.cancelExportJob(id);
        return ResponseWrapper.ok("Export job cancelled successfully", null);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry failed export job", description = "Retry a failed export job")
    public ResponseEntity<CommonResponse<DocumentExportJobDTO>> retryFailedJob(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /dms/export/{}/retry - Retrying export job", id);
        DocumentExportJobDTO response = exportService.retryFailedJob(id, userId);
        return ResponseWrapper.ok("Export job retry started", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete export job", description = "Delete an export job and its files")
    public ResponseEntity<CommonResponse<Void>> deleteExportJob(@PathVariable Long id) {
        log.info("DELETE /dms/export/{} - Deleting export job", id);
        exportService.deleteExportJob(id);
        return ResponseWrapper.ok("Export job deleted successfully", null);
    }

    // ==================== DOWNLOAD ====================

    @GetMapping("/{id}/download")
    @Operation(summary = "Download export file", description = "Download the exported file")
    public ResponseEntity<byte[]> downloadExportFile(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/export/{}/download - Downloading export file", id);
        byte[] fileContent = exportService.downloadExportFile(id, userId);
        DocumentExportJobDTO job = exportService.getExportJobById(id);

        String filename = job.getJobId() + "." +
                (job.getTotalDocuments() > 1 ? "zip" : job.getExportFormat().toString().toLowerCase());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileContent);
    }

    @GetMapping("/jobs/{jobId}/download")
    @Operation(summary = "Download export file by job ID", description = "Download the exported file using job ID")
    public ResponseEntity<byte[]> downloadExportFileByJobId(
            @PathVariable String jobId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/export/jobs/{}/download - Downloading export file", jobId);
        DocumentExportJobDTO job = exportService.getExportJobByJobId(jobId);
        byte[] fileContent = exportService.downloadExportFile(job.getId(), userId);

        String filename = job.getJobId() + "." +
                (job.getTotalDocuments() > 1 ? "zip" : job.getExportFormat().toString().toLowerCase());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileContent);
    }

    @GetMapping("/{id}/download-url")
    @Operation(summary = "Get download URL", description = "Get a pre-signed download URL")
    public ResponseEntity<CommonResponse<String>> getDownloadUrl(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/export/{}/download-url - Getting download URL", id);
        String url = exportService.getDownloadUrl(id, userId);
        return ResponseWrapper.ok("Download URL generated", url);
    }

    // ==================== HISTORY ====================

    @GetMapping("/history")
    @Operation(summary = "Get export history", description = "Get export history for current user")
    public ResponseEntity<CommonResponse<Page<ExportHistoryDTO>>> getExportHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/export/history - Fetching export history for user: {}", userId);
        Page<ExportHistoryDTO> history = exportService.getExportHistory(userId, pageable);
        return ResponseWrapper.ok("Export history retrieved successfully", history);
    }

    @GetMapping("/history/date-range")
    @Operation(summary = "Get export history by date range", description = "Get export history within date range")
    public ResponseEntity<CommonResponse<Page<ExportHistoryDTO>>> getExportHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/export/history/date-range - Fetching history from {} to {}", startDate, endDate);
        Page<ExportHistoryDTO> history = exportService.getExportHistoryByDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("Export history retrieved successfully", history);
    }

    // ==================== SUMMARY ====================

    @GetMapping("/summary")
    @Operation(summary = "Get export summary", description = "Get overall export statistics")
    public ResponseEntity<CommonResponse<ExportSummaryDTO>> getExportSummary() {
        log.info("GET /dms/export/summary - Fetching export summary");
        ExportSummaryDTO summary = exportService.getExportSummary();
        return ResponseWrapper.ok("Export summary retrieved successfully", summary);
    }

    @GetMapping("/summary/my")
    @Operation(summary = "Get my export summary", description = "Get export statistics for current user")
    public ResponseEntity<CommonResponse<ExportSummaryDTO>> getMyExportSummary(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/export/summary/my - Fetching export summary for user: {}", userId);
        ExportSummaryDTO summary = exportService.getExportSummaryByUser(userId);
        return ResponseWrapper.ok("Export summary retrieved successfully", summary);
    }
}

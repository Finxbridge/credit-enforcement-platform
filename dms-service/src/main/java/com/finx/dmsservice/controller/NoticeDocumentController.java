package com.finx.dmsservice.controller;

import com.finx.dmsservice.domain.dto.CommonResponse;
import com.finx.dmsservice.domain.dto.NoticeDocumentDTO;
import com.finx.dmsservice.domain.dto.NoticeSearchCriteria;
import com.finx.dmsservice.domain.dto.NoticeSummaryDTO;
import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import com.finx.dmsservice.service.NoticeDocumentService;
import com.finx.dmsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/dms/notices")
@RequiredArgsConstructor
@Tag(name = "Notice Documents", description = "APIs for notice document repository management")
public class NoticeDocumentController {

    private final NoticeDocumentService noticeDocumentService;

    // ==================== RETRIEVAL ====================

    @GetMapping
    @Operation(summary = "Get all notices", description = "Get all notice documents with pagination")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getAllNotices(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices - Fetching all notices");
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getAllNotices(pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notice by ID", description = "Get notice document details by ID")
    public ResponseEntity<CommonResponse<NoticeDocumentDTO>> getNoticeById(@PathVariable Long id) {
        log.info("GET /dms/notices/{} - Fetching notice", id);
        NoticeDocumentDTO notice = noticeDocumentService.getNoticeById(id);
        return ResponseWrapper.ok("Notice retrieved successfully", notice);
    }

    @GetMapping("/number/{noticeNumber}")
    @Operation(summary = "Get notice by number", description = "Get notice document by notice number")
    public ResponseEntity<CommonResponse<NoticeDocumentDTO>> getNoticeByNumber(
            @PathVariable String noticeNumber) {
        log.info("GET /dms/notices/number/{} - Fetching notice", noticeNumber);
        NoticeDocumentDTO notice = noticeDocumentService.getNoticeByNumber(noticeNumber);
        return ResponseWrapper.ok("Notice retrieved successfully", notice);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get notices by case", description = "Get all notices for a case")
    public ResponseEntity<CommonResponse<List<NoticeDocumentDTO>>> getNoticesByCaseId(
            @PathVariable Long caseId) {
        log.info("GET /dms/notices/case/{} - Fetching case notices", caseId);
        List<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByCaseId(caseId);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/loan/{loanAccountNumber}")
    @Operation(summary = "Get notices by loan", description = "Get all notices for a loan account")
    public ResponseEntity<CommonResponse<List<NoticeDocumentDTO>>> getNoticesByLoanAccount(
            @PathVariable String loanAccountNumber) {
        log.info("GET /dms/notices/loan/{} - Fetching loan notices", loanAccountNumber);
        List<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByLoanAccount(loanAccountNumber);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get notices by customer", description = "Get all notices for a customer")
    public ResponseEntity<CommonResponse<List<NoticeDocumentDTO>>> getNoticesByCustomerId(
            @PathVariable Long customerId) {
        log.info("GET /dms/notices/customer/{} - Fetching customer notices", customerId);
        List<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByCustomerId(customerId);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get notices by type", description = "Get notices by notice type")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByType(
            @PathVariable NoticeType type,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/type/{} - Fetching notices", type);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByType(type, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get notices by status", description = "Get notices by notice status")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByStatus(
            @PathVariable NoticeStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/status/{} - Fetching notices", status);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByStatus(status, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/type/{type}/status/{status}")
    @Operation(summary = "Get notices by type and status", description = "Get notices filtered by type and status")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByTypeAndStatus(
            @PathVariable NoticeType type,
            @PathVariable NoticeStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/type/{}/status/{} - Fetching notices", type, status);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByTypeAndStatus(type, status, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Get notices by region", description = "Get notices by region")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByRegion(
            @PathVariable String region,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/region/{} - Fetching notices", region);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByRegion(region, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/product/{productType}")
    @Operation(summary = "Get notices by product type", description = "Get notices by product type")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByProductType(
            @PathVariable String productType,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/product/{} - Fetching notices", productType);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByProductType(productType, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/vendor/{vendorId}")
    @Operation(summary = "Get notices by vendor", description = "Get notices by dispatch vendor")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByVendor(
            @PathVariable Long vendorId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/vendor/{} - Fetching notices", vendorId);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByVendor(vendorId, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get notices by date range", description = "Get notices generated within date range")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getNoticesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/date-range - Fetching notices from {} to {}", startDate, endDate);
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getNoticesByDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    // ==================== SEARCH ====================

    @PostMapping("/search")
    @Operation(summary = "Search notices", description = "Search notices with multiple criteria")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> searchNotices(
            @RequestBody NoticeSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("POST /dms/notices/search - Searching notices");
        Page<NoticeDocumentDTO> notices = noticeDocumentService.searchNotices(criteria, pageable);
        return ResponseWrapper.ok("Search completed successfully", notices);
    }

    // ==================== PREVIEW & DOWNLOAD ====================

    @GetMapping("/{id}/preview")
    @Operation(summary = "Preview notice", description = "Preview notice document")
    public ResponseEntity<byte[]> previewNotice(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/notices/{}/preview - Previewing notice", id);
        byte[] content = noticeDocumentService.previewNotice(id, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    @GetMapping("/{id}/preview-url")
    @Operation(summary = "Get preview URL", description = "Get pre-signed URL for notice preview")
    public ResponseEntity<CommonResponse<String>> getNoticePreviewUrl(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/notices/{}/preview-url - Getting preview URL", id);
        String url = noticeDocumentService.getNoticePreviewUrl(id, userId);
        return ResponseWrapper.ok("Preview URL generated", url);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download notice", description = "Download notice document")
    public ResponseEntity<byte[]> downloadNotice(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /dms/notices/{}/download - Downloading notice", id);
        byte[] content = noticeDocumentService.downloadNotice(id, userId);

        NoticeDocumentDTO notice = noticeDocumentService.getNoticeById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                notice.getFileName() != null ? notice.getFileName() : "notice-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    // ==================== VERSIONS ====================

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get notice versions", description = "Get all versions of a notice")
    public ResponseEntity<CommonResponse<List<NoticeDocumentDTO>>> getNoticeVersions(
            @PathVariable Long id) {
        log.info("GET /dms/notices/{}/versions - Fetching notice versions", id);
        List<NoticeDocumentDTO> versions = noticeDocumentService.getNoticeVersions(id);
        return ResponseWrapper.ok("Notice versions retrieved successfully", versions);
    }

    // ==================== SUMMARY & STATS ====================

    @GetMapping("/summary")
    @Operation(summary = "Get notice summary", description = "Get overall notice statistics")
    public ResponseEntity<CommonResponse<NoticeSummaryDTO>> getNoticeSummary() {
        log.info("GET /dms/notices/summary - Fetching notice summary");
        NoticeSummaryDTO summary = noticeDocumentService.getNoticeSummary();
        return ResponseWrapper.ok("Notice summary retrieved successfully", summary);
    }

    @GetMapping("/summary/date-range")
    @Operation(summary = "Get notice summary by date range", description = "Get notice statistics for date range")
    public ResponseEntity<CommonResponse<NoticeSummaryDTO>> getNoticeSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /dms/notices/summary/date-range - Fetching summary from {} to {}", startDate, endDate);
        NoticeSummaryDTO summary = noticeDocumentService.getNoticeSummaryByDateRange(startDate, endDate);
        return ResponseWrapper.ok("Notice summary retrieved successfully", summary);
    }

    @GetMapping("/case/{caseId}/count")
    @Operation(summary = "Count notices by case", description = "Get notice count for a case")
    public ResponseEntity<CommonResponse<Long>> countNoticesByCaseId(@PathVariable Long caseId) {
        log.info("GET /dms/notices/case/{}/count - Counting notices", caseId);
        Long count = noticeDocumentService.countNoticesByCaseId(caseId);
        return ResponseWrapper.ok("Notice count retrieved", count);
    }

    @GetMapping("/status/{status}/count")
    @Operation(summary = "Count notices by status", description = "Get notice count by status")
    public ResponseEntity<CommonResponse<Long>> countNoticesByStatus(@PathVariable NoticeStatus status) {
        log.info("GET /dms/notices/status/{}/count - Counting notices", status);
        Long count = noticeDocumentService.countNoticesByStatus(status);
        return ResponseWrapper.ok("Notice count retrieved", count);
    }

    @GetMapping("/type/{type}/count")
    @Operation(summary = "Count notices by type", description = "Get notice count by type")
    public ResponseEntity<CommonResponse<Long>> countNoticesByType(@PathVariable NoticeType type) {
        log.info("GET /dms/notices/type/{}/count - Counting notices", type);
        Long count = noticeDocumentService.countNoticesByType(type);
        return ResponseWrapper.ok("Notice count retrieved", count);
    }

    // ==================== OVERDUE ====================

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue responses", description = "Get notices with overdue responses")
    public ResponseEntity<CommonResponse<Page<NoticeDocumentDTO>>> getOverdueResponses(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /dms/notices/overdue - Fetching overdue notices");
        Page<NoticeDocumentDTO> notices = noticeDocumentService.getOverdueResponses(pageable);
        return ResponseWrapper.ok("Overdue notices retrieved successfully", notices);
    }

    @GetMapping("/overdue/count")
    @Operation(summary = "Count overdue responses", description = "Get count of notices with overdue responses")
    public ResponseEntity<CommonResponse<Long>> countOverdueResponses() {
        log.info("GET /dms/notices/overdue/count - Counting overdue notices");
        Long count = noticeDocumentService.countOverdueResponses();
        return ResponseWrapper.ok("Overdue count retrieved", count);
    }
}

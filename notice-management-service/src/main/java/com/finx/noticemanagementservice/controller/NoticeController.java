package com.finx.noticemanagementservice.controller;

import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
import com.finx.noticemanagementservice.domain.enums.NoticeType;
import com.finx.noticemanagementservice.service.NoticeService;
import com.finx.noticemanagementservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public ResponseEntity<CommonResponse<NoticeDTO>> createNotice(
            @Valid @RequestBody CreateNoticeRequest request) {
        NoticeDTO notice = noticeService.createNotice(request);
        return ResponseWrapper.created("Notice created successfully", notice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<NoticeDTO>> getNoticeById(@PathVariable Long id) {
        NoticeDTO notice = noticeService.getNoticeById(id);
        return ResponseWrapper.ok("Notice retrieved successfully", notice);
    }

    @GetMapping("/number/{noticeNumber}")
    public ResponseEntity<CommonResponse<NoticeDTO>> getNoticeByNumber(@PathVariable String noticeNumber) {
        NoticeDTO notice = noticeService.getNoticeByNumber(noticeNumber);
        return ResponseWrapper.ok("Notice retrieved successfully", notice);
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<CommonResponse<List<NoticeDTO>>> getNoticesByCaseId(@PathVariable Long caseId) {
        List<NoticeDTO> notices = noticeService.getNoticesByCaseId(caseId);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/loan/{loanAccountNumber}")
    public ResponseEntity<CommonResponse<List<NoticeDTO>>> getNoticesByLoanAccountNumber(
            @PathVariable String loanAccountNumber) {
        List<NoticeDTO> notices = noticeService.getNoticesByLoanAccountNumber(loanAccountNumber);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<CommonResponse<Page<NoticeDTO>>> getNoticesByStatus(
            @PathVariable NoticeStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeDTO> notices = noticeService.getNoticesByStatus(status, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<Page<NoticeDTO>>> getNoticesByType(
            @PathVariable NoticeType type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeDTO> notices = noticeService.getNoticesByType(type, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<CommonResponse<Page<NoticeDTO>>> getNoticesByVendor(
            @PathVariable Long vendorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeDTO> notices = noticeService.getNoticesByVendor(vendorId, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping("/date-range")
    public ResponseEntity<CommonResponse<Page<NoticeDTO>>> getNoticesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeDTO> notices = noticeService.getNoticesByDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<NoticeDTO>>> getAllNotices(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeDTO> notices = noticeService.getAllNotices(pageable);
        return ResponseWrapper.ok("Notices retrieved successfully", notices);
    }

    @PostMapping("/generate")
    public ResponseEntity<CommonResponse<NoticeDTO>> generateNotice(
            @Valid @RequestBody GenerateNoticeRequest request) {
        NoticeDTO notice = noticeService.generateNotice(request);
        return ResponseWrapper.ok("Notice generated successfully", notice);
    }

    @PostMapping("/dispatch")
    public ResponseEntity<CommonResponse<NoticeDTO>> dispatchNotice(
            @Valid @RequestBody DispatchNoticeRequest request) {
        NoticeDTO notice = noticeService.dispatchNotice(request);
        return ResponseWrapper.ok("Notice dispatched successfully", notice);
    }

    @PutMapping("/delivery-status")
    public ResponseEntity<CommonResponse<NoticeDTO>> updateDeliveryStatus(
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        NoticeDTO notice = noticeService.updateDeliveryStatus(request);
        return ResponseWrapper.ok("Delivery status updated successfully", notice);
    }

    @PutMapping("/{noticeId}/mark-delivered")
    public ResponseEntity<CommonResponse<NoticeDTO>> markAsDelivered(
            @PathVariable Long noticeId,
            @RequestParam Long podId) {
        NoticeDTO notice = noticeService.markAsDelivered(noticeId, podId);
        return ResponseWrapper.ok("Notice marked as delivered", notice);
    }

    @PutMapping("/{noticeId}/mark-rto")
    public ResponseEntity<CommonResponse<NoticeDTO>> markAsRto(
            @PathVariable Long noticeId,
            @RequestParam String rtoReason) {
        NoticeDTO notice = noticeService.markAsRto(noticeId, rtoReason);
        return ResponseWrapper.ok("Notice marked as RTO", notice);
    }

    @GetMapping("/stats")
    public ResponseEntity<CommonResponse<NoticeStatsDTO>> getNoticeStats() {
        NoticeStatsDTO stats = noticeService.getNoticeStats();
        return ResponseWrapper.ok("Notice stats retrieved successfully", stats);
    }

    @GetMapping("/sla-breaches/dispatch")
    public ResponseEntity<CommonResponse<List<NoticeDTO>>> getDispatchSlaBreaches() {
        List<NoticeDTO> notices = noticeService.getDispatchSlaBreaches();
        return ResponseWrapper.ok("Dispatch SLA breaches retrieved successfully", notices);
    }

    @GetMapping("/sla-breaches/delivery")
    public ResponseEntity<CommonResponse<List<NoticeDTO>>> getDeliverySlaBreaches() {
        List<NoticeDTO> notices = noticeService.getDeliverySlaBreaches();
        return ResponseWrapper.ok("Delivery SLA breaches retrieved successfully", notices);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseWrapper.okMessage("Notice deleted successfully");
    }
}

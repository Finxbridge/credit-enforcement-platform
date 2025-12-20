package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.service.PTPService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collections/ptp")
@RequiredArgsConstructor
@Tag(name = "PTP Management", description = "APIs for Promise to Pay management")
public class PTPController {

    private final PTPService ptpService;

    @PostMapping
    @Operation(summary = "Capture PTP commitment", description = "Record a Promise to Pay commitment from borrower")
    public ResponseEntity<CommonResponse<PTPResponse>> capturePTP(
            @Valid @RequestBody CapturePTPRequest request) {
        log.info("POST /ptp - Capturing PTP for case: {}", request.getCaseId());
        PTPResponse response = ptpService.capturePTP(request);
        return ResponseWrapper.created("PTP captured successfully", response);
    }

    @GetMapping("/due")
    @Operation(summary = "Get PTPs due", description = "Get all PTPs due on a specific date")
    public ResponseEntity<CommonResponse<List<PTPCaseDTO>>> getPTPsDue(
            @RequestParam(required = false) LocalDate dueDate,
            @RequestParam(required = false) Long userId) {
        LocalDate targetDate = dueDate != null ? dueDate : LocalDate.now();
        log.info("GET /ptp/due - Fetching PTPs due on: {} for user: {}", targetDate, userId);
        List<PTPCaseDTO> ptps = ptpService.getPTPsDue(targetDate, userId);
        return ResponseWrapper.ok("PTPs due retrieved successfully", ptps);
    }

    @GetMapping("/broken")
    @Operation(summary = "Get broken PTPs", description = "Get all PTPs that are past due date")
    public ResponseEntity<CommonResponse<List<PTPCaseDTO>>> getBrokenPTPs(
            @RequestParam(required = false) Long userId) {
        log.info("GET /ptp/broken - Fetching broken PTPs for user: {}", userId);
        List<PTPCaseDTO> ptps = ptpService.getBrokenPTPs(userId);
        return ResponseWrapper.ok("Broken PTPs retrieved successfully", ptps);
    }

    @GetMapping("/{ptpId}")
    @Operation(summary = "Get PTP details", description = "Get details of a specific PTP commitment")
    public ResponseEntity<CommonResponse<PTPResponse>> getPTPById(@PathVariable Long ptpId) {
        log.info("GET /ptp/{} - Fetching PTP details", ptpId);
        PTPResponse response = ptpService.getPTPById(ptpId);
        return ResponseWrapper.ok("PTP details retrieved successfully", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get case PTPs", description = "Get all PTP commitments for a specific case")
    public ResponseEntity<CommonResponse<List<PTPResponse>>> getCasePTPs(@PathVariable Long caseId) {
        log.info("GET /ptp/case/{} - Fetching PTPs for case", caseId);
        List<PTPResponse> ptps = ptpService.getPTPsByCase(caseId);
        return ResponseWrapper.ok("Case PTPs retrieved successfully", ptps);
    }

    @PutMapping("/{ptpId}")
    @Operation(summary = "Update PTP status", description = "Mark PTP as KEPT, BROKEN, RENEWED, etc.")
    public ResponseEntity<CommonResponse<PTPResponse>> updatePTPStatus(
            @PathVariable Long ptpId,
            @Valid @RequestBody UpdatePTPRequest request) {
        log.info("PUT /ptp/{} - Updating PTP status to: {}", ptpId, request.getPtpStatus());
        PTPResponse response = ptpService.updatePTPStatus(ptpId, request);
        return ResponseWrapper.ok("PTP status updated successfully", response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get PTP statistics", description = "Get PTP performance metrics and statistics")
    public ResponseEntity<CommonResponse<PTPStatsDTO>> getPTPStats(
            @RequestParam(required = false) Long userId) {
        log.info("GET /ptp/stats - Fetching PTP statistics for user: {}", userId);
        PTPStatsDTO stats = ptpService.getPTPStats(userId);
        return ResponseWrapper.ok("PTP statistics retrieved successfully", stats);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get PTPs by status", description = "Get PTPs filtered by status with pagination")
    public ResponseEntity<CommonResponse<Page<PTPResponse>>> getPTPsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /ptp/status/{} - Fetching PTPs", status);
        Page<PTPResponse> ptps = ptpService.getPTPsByStatus(status, pageable);
        return ResponseWrapper.ok("PTPs retrieved successfully", ptps);
    }

    @PostMapping("/process-overdue")
    @Operation(summary = "Process overdue PTPs", description = "Auto-mark overdue PTPs as BROKEN")
    public ResponseEntity<CommonResponse<Integer>> processOverduePTPs() {
        log.info("POST /ptp/process-overdue - Processing overdue PTPs");
        Integer count = ptpService.processOverduePTPs();
        return ResponseWrapper.ok("Processed " + count + " overdue PTPs", count);
    }

    @PostMapping("/send-reminders")
    @Operation(summary = "Send PTP reminders", description = "Send reminders for PTPs due tomorrow")
    public ResponseEntity<CommonResponse<Integer>> sendPTPReminders() {
        log.info("POST /ptp/send-reminders - Sending PTP reminders");
        Integer count = ptpService.sendPTPReminders();
        return ResponseWrapper.ok("Sent " + count + " PTP reminders", count);
    }
}

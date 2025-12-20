package com.finx.myworkflow.controller;

import com.finx.myworkflow.domain.dto.CaseSummaryDTO;
import com.finx.myworkflow.domain.dto.CaseTabsDataDTO;
import com.finx.myworkflow.domain.dto.CommonResponse;
import com.finx.myworkflow.service.CaseDataService;
import com.finx.myworkflow.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow/cases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Case Data", description = "APIs for fetching case data for different tabs")
public class CaseDataController {

    private final CaseDataService caseDataService;

    @GetMapping
    @Operation(summary = "Get cases for workflow", description = "Get cases list for workflow - shows all cases for admin, allocated cases for collectors")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getCasesForWorkflow(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "COLLECTOR") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("User {} with role {} getting cases for workflow - page: {}, size: {}", userId, userRole, page, size);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole) || "SUPER_ADMIN".equalsIgnoreCase(userRole);
        Map<String, Object> cases = caseDataService.getCasesForWorkflow(userId, isAdmin, page, size);
        return ResponseWrapper.ok("Cases retrieved successfully", cases);
    }

    @GetMapping("/{caseId}/summary")
    @Operation(summary = "Get case summary", description = "Get case summary with key information")
    public ResponseEntity<CommonResponse<CaseSummaryDTO>> getCaseSummary(
            @PathVariable Long caseId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("User {} getting summary for case: {}", userId, caseId);
        CaseSummaryDTO summary = caseDataService.getCaseSummary(caseId, userId);
        if (summary == null) {
            return ResponseWrapper.notFound("Case not found: " + caseId);
        }
        return ResponseWrapper.ok("Case summary retrieved", summary);
    }

    @GetMapping("/{caseId}/tabs")
    @Operation(summary = "Get all tabs data", description = "Get data for all tabs of a case")
    public ResponseEntity<CommonResponse<CaseTabsDataDTO>> getAllTabsData(
            @PathVariable Long caseId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("User {} getting all tabs data for case: {}", userId, caseId);
        CaseTabsDataDTO tabsData = caseDataService.getAllTabsData(caseId, userId);
        return ResponseWrapper.ok("Case tabs data retrieved", tabsData);
    }

    @GetMapping("/{caseId}/loan-details")
    @Operation(summary = "Get loan details", description = "Get loan details tab data")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getLoanDetails(
            @PathVariable Long caseId) {
        log.info("Getting loan details for case: {}", caseId);
        Map<String, Object> loanDetails = caseDataService.getLoanDetails(caseId);
        return ResponseWrapper.ok("Loan details retrieved", loanDetails);
    }

    @GetMapping("/{caseId}/customer-details")
    @Operation(summary = "Get customer details", description = "Get customer details tab data")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getCustomerDetails(
            @PathVariable Long caseId) {
        log.info("Getting customer details for case: {}", caseId);
        Map<String, Object> customerDetails = caseDataService.getCustomerDetails(caseId);
        return ResponseWrapper.ok("Customer details retrieved", customerDetails);
    }

    @GetMapping("/{caseId}/repayments")
    @Operation(summary = "Get repayment history", description = "Get repayment history for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getRepaymentHistory(
            @PathVariable Long caseId) {
        log.info("Getting repayment history for case: {}", caseId);
        List<Map<String, Object>> repayments = caseDataService.getRepaymentHistory(caseId);
        return ResponseWrapper.ok("Repayment history retrieved", repayments);
    }

    @GetMapping("/{caseId}/ptps")
    @Operation(summary = "Get PTP history", description = "Get PTP history for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getPtpHistory(
            @PathVariable Long caseId) {
        log.info("Getting PTP history for case: {}", caseId);
        List<Map<String, Object>> ptps = caseDataService.getPtpHistory(caseId);
        return ResponseWrapper.ok("PTP history retrieved", ptps);
    }

    @GetMapping("/{caseId}/notices")
    @Operation(summary = "Get notices", description = "Get notices for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getNotices(
            @PathVariable Long caseId) {
        log.info("Getting notices for case: {}", caseId);
        List<Map<String, Object>> notices = caseDataService.getNotices(caseId);
        return ResponseWrapper.ok("Notices retrieved", notices);
    }

    @GetMapping("/{caseId}/calls")
    @Operation(summary = "Get call logs", description = "Get call logs for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getCallLogs(
            @PathVariable Long caseId) {
        log.info("Getting call logs for case: {}", caseId);
        List<Map<String, Object>> callLogs = caseDataService.getCallLogs(caseId);
        return ResponseWrapper.ok("Call logs retrieved", callLogs);
    }

    @GetMapping("/{caseId}/sms")
    @Operation(summary = "Get SMS history", description = "Get SMS history for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getSmsHistory(
            @PathVariable Long caseId) {
        log.info("Getting SMS history for case: {}", caseId);
        List<Map<String, Object>> smsHistory = caseDataService.getSmsHistory(caseId);
        return ResponseWrapper.ok("SMS history retrieved", smsHistory);
    }

    @GetMapping("/{caseId}/emails")
    @Operation(summary = "Get email history", description = "Get email history for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getEmailHistory(
            @PathVariable Long caseId) {
        log.info("Getting email history for case: {}", caseId);
        List<Map<String, Object>> emailHistory = caseDataService.getEmailHistory(caseId);
        return ResponseWrapper.ok("Email history retrieved", emailHistory);
    }

    @GetMapping("/{caseId}/documents")
    @Operation(summary = "Get documents", description = "Get documents for a case")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getDocuments(
            @PathVariable Long caseId) {
        log.info("Getting documents for case: {}", caseId);
        List<Map<String, Object>> documents = caseDataService.getDocuments(caseId);
        return ResponseWrapper.ok("Documents retrieved", documents);
    }
}

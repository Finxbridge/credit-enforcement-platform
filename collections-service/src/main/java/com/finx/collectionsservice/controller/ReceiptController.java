package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import com.finx.collectionsservice.service.ReceiptService;
import com.finx.collectionsservice.util.ResponseWrapper;
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
@RequestMapping("/collections/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "APIs for repayment receipt management")
public class ReceiptController {

    private final ReceiptService receiptService;

    // ==================== GENERATION ====================

    @PostMapping("/generate")
    @Operation(summary = "Generate receipt", description = "Generate receipt for approved repayment with advanced options")
    public ResponseEntity<CommonResponse<ReceiptDTO>> generateReceipt(
            @Valid @RequestBody GenerateReceiptRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/generate - Generating receipt for repayment: {}", request.getRepaymentId());
        ReceiptDTO response = receiptService.generateReceipt(request, userId);
        return ResponseWrapper.created("Receipt generated successfully", response);
    }

    @PostMapping("/generate/simple")
    @Operation(summary = "Generate receipt (simple)", description = "Generate receipt for approved repayment")
    public ResponseEntity<CommonResponse<ReceiptDTO>> generateReceiptSimple(
            @RequestParam Long repaymentId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/generate/simple - Generating receipt for repayment: {}", repaymentId);
        ReceiptDTO response = receiptService.generateReceipt(repaymentId, userId);
        return ResponseWrapper.created("Receipt generated successfully", response);
    }

    @PostMapping("/generate/bulk")
    @Operation(summary = "Generate receipts in bulk", description = "Generate receipts for multiple repayments")
    public ResponseEntity<CommonResponse<BulkReceiptResponse>> generateBulkReceipts(
            @Valid @RequestBody BulkReceiptRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/generate/bulk - Generating {} receipts", request.getRepaymentIds().size());
        BulkReceiptResponse response = receiptService.generateBulkReceipts(request, userId);
        return ResponseWrapper.created("Bulk receipt generation completed", response);
    }

    // ==================== RETRIEVAL ====================

    @GetMapping("/{receiptId}")
    @Operation(summary = "Get receipt by ID", description = "Get receipt details")
    public ResponseEntity<CommonResponse<ReceiptDTO>> getReceipt(@PathVariable Long receiptId) {
        log.info("GET /receipts/{} - Fetching receipt", receiptId);
        ReceiptDTO response = receiptService.getReceiptById(receiptId);
        return ResponseWrapper.ok("Receipt retrieved successfully", response);
    }

    @GetMapping("/number/{receiptNumber}")
    @Operation(summary = "Get receipt by number", description = "Get receipt by receipt number")
    public ResponseEntity<CommonResponse<ReceiptDTO>> getReceiptByNumber(
            @PathVariable String receiptNumber) {
        log.info("GET /receipts/number/{} - Fetching receipt", receiptNumber);
        ReceiptDTO response = receiptService.getReceiptByNumber(receiptNumber);
        return ResponseWrapper.ok("Receipt retrieved successfully", response);
    }

    @GetMapping("/repayment/{repaymentId}")
    @Operation(summary = "Get receipt by repayment", description = "Get receipt for a repayment")
    public ResponseEntity<CommonResponse<ReceiptDTO>> getReceiptByRepayment(
            @PathVariable Long repaymentId) {
        log.info("GET /receipts/repayment/{} - Fetching receipt", repaymentId);
        ReceiptDTO response = receiptService.getReceiptByRepaymentId(repaymentId);
        return ResponseWrapper.ok("Receipt retrieved successfully", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get case receipts", description = "Get all receipts for a case")
    public ResponseEntity<CommonResponse<List<ReceiptDTO>>> getReceiptsByCaseId(
            @PathVariable Long caseId) {
        log.info("GET /receipts/case/{} - Fetching case receipts", caseId);
        List<ReceiptDTO> receipts = receiptService.getReceiptsByCaseId(caseId);
        return ResponseWrapper.ok("Receipts retrieved successfully", receipts);
    }

    @GetMapping("/loan/{loanAccountNumber}")
    @Operation(summary = "Get receipts by loan account", description = "Get all receipts for a loan account")
    public ResponseEntity<CommonResponse<List<ReceiptDTO>>> getReceiptsByLoanAccount(
            @PathVariable String loanAccountNumber) {
        log.info("GET /receipts/loan/{} - Fetching loan receipts", loanAccountNumber);
        List<ReceiptDTO> receipts = receiptService.getReceiptsByLoanAccount(loanAccountNumber);
        return ResponseWrapper.ok("Receipts retrieved successfully", receipts);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get receipts by status", description = "Get receipts by status")
    public ResponseEntity<CommonResponse<Page<ReceiptDTO>>> getReceiptsByStatus(
            @PathVariable ReceiptStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /receipts/status/{} - Fetching receipts", status);
        Page<ReceiptDTO> receipts = receiptService.getReceiptsByStatus(status, pageable);
        return ResponseWrapper.ok("Receipts retrieved successfully", receipts);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get receipts by date range", description = "Get receipts within date range")
    public ResponseEntity<CommonResponse<Page<ReceiptDTO>>> getReceiptsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /receipts/date-range - Fetching receipts from {} to {}", startDate, endDate);
        Page<ReceiptDTO> receipts = receiptService.getReceiptsByDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("Receipts retrieved successfully", receipts);
    }

    @PostMapping("/search")
    @Operation(summary = "Search receipts", description = "Search receipts with multiple criteria")
    public ResponseEntity<CommonResponse<Page<ReceiptDTO>>> searchReceipts(
            @RequestBody ReceiptSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("POST /receipts/search - Searching receipts");
        Page<ReceiptDTO> receipts = receiptService.searchReceipts(criteria, pageable);
        return ResponseWrapper.ok("Search completed successfully", receipts);
    }

    @GetMapping("/my-receipts")
    @Operation(summary = "Get my generated receipts", description = "Get receipts generated by current user")
    public ResponseEntity<CommonResponse<Page<ReceiptDTO>>> getMyGeneratedReceipts(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /receipts/my-receipts - Fetching receipts for user {}", userId);
        Page<ReceiptDTO> receipts = receiptService.getMyGeneratedReceipts(userId, pageable);
        return ResponseWrapper.ok("Receipts retrieved successfully", receipts);
    }

    // ==================== ACTIONS ====================

    @PostMapping("/{receiptId}/download")
    @Operation(summary = "Download receipt", description = "Mark receipt as downloaded and return details")
    public ResponseEntity<CommonResponse<ReceiptDTO>> downloadReceipt(
            @PathVariable Long receiptId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/download - Downloading receipt", receiptId);
        ReceiptDTO response = receiptService.downloadReceipt(receiptId, userId);
        return ResponseWrapper.ok("Receipt downloaded", response);
    }

    @GetMapping("/{receiptId}/download/pdf")
    @Operation(summary = "Download receipt PDF", description = "Download receipt as PDF file")
    public ResponseEntity<byte[]> downloadReceiptPdf(
            @PathVariable Long receiptId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /receipts/{}/download/pdf - Downloading receipt PDF", receiptId);
        byte[] pdfContent = receiptService.downloadReceiptPdf(receiptId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + receiptId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }

    @PostMapping("/{receiptId}/email")
    @Operation(summary = "Email receipt", description = "Email receipt to specified address")
    public ResponseEntity<CommonResponse<ReceiptDTO>> emailReceipt(
            @PathVariable Long receiptId,
            @RequestParam String email,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/email - Emailing receipt to {}", receiptId, email);
        ReceiptDTO response = receiptService.emailReceipt(receiptId, email, userId);
        return ResponseWrapper.ok("Receipt emailed successfully", response);
    }

    @PostMapping("/{receiptId}/email/advanced")
    @Operation(summary = "Email receipt (advanced)", description = "Email receipt with CC and custom message")
    public ResponseEntity<CommonResponse<ReceiptDTO>> emailReceiptAdvanced(
            @PathVariable Long receiptId,
            @Valid @RequestBody EmailReceiptRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/email/advanced - Emailing receipt to {}", receiptId, request.getPrimaryEmail());
        ReceiptDTO response = receiptService.emailReceipt(receiptId, request, userId);
        return ResponseWrapper.ok("Receipt emailed successfully", response);
    }

    @PostMapping("/{receiptId}/sms")
    @Operation(summary = "Send receipt SMS", description = "Send receipt notification via SMS")
    public ResponseEntity<CommonResponse<ReceiptDTO>> sendReceiptSms(
            @PathVariable Long receiptId,
            @RequestParam String phoneNumber,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/sms - Sending SMS to {}", receiptId, phoneNumber);
        ReceiptDTO response = receiptService.sendReceiptSms(receiptId, phoneNumber, userId);
        return ResponseWrapper.ok("Receipt SMS sent successfully", response);
    }

    @PostMapping("/{receiptId}/verify")
    @Operation(summary = "Verify receipt", description = "Verify a generated receipt")
    public ResponseEntity<CommonResponse<ReceiptDTO>> verifyReceipt(
            @PathVariable Long receiptId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/verify - Verifying receipt", receiptId);
        ReceiptDTO response = receiptService.verifyReceipt(receiptId, userId);
        return ResponseWrapper.ok("Receipt verified successfully", response);
    }

    @PostMapping("/{receiptId}/cancel")
    @Operation(summary = "Cancel receipt", description = "Cancel a receipt")
    public ResponseEntity<CommonResponse<ReceiptDTO>> cancelReceipt(
            @PathVariable Long receiptId,
            @Valid @RequestBody CancelReceiptRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/cancel - Cancelling receipt", receiptId);
        ReceiptDTO response = receiptService.cancelReceipt(receiptId, request, userId);
        return ResponseWrapper.ok("Receipt cancelled successfully", response);
    }

    @PostMapping("/{receiptId}/void")
    @Operation(summary = "Void receipt", description = "Void a receipt (requires approval)")
    public ResponseEntity<CommonResponse<ReceiptDTO>> voidReceipt(
            @PathVariable Long receiptId,
            @Valid @RequestBody VoidReceiptRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/void - Voiding receipt", receiptId);
        ReceiptDTO response = receiptService.voidReceipt(receiptId, request, userId);
        return ResponseWrapper.ok("Receipt voided successfully", response);
    }

    @PostMapping("/{receiptId}/regenerate")
    @Operation(summary = "Regenerate receipt", description = "Regenerate receipt PDF")
    public ResponseEntity<CommonResponse<ReceiptDTO>> regenerateReceipt(
            @PathVariable Long receiptId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /receipts/{}/regenerate - Regenerating receipt", receiptId);
        ReceiptDTO response = receiptService.regenerateReceipt(receiptId, userId);
        return ResponseWrapper.ok("Receipt regenerated successfully", response);
    }

    // ==================== HISTORY ====================

    @GetMapping("/{receiptId}/history")
    @Operation(summary = "Get receipt history", description = "Get audit history for a receipt")
    public ResponseEntity<CommonResponse<List<ReceiptHistoryDTO>>> getReceiptHistory(
            @PathVariable Long receiptId) {
        log.info("GET /receipts/{}/history - Fetching receipt history", receiptId);
        List<ReceiptHistoryDTO> history = receiptService.getReceiptHistory(receiptId);
        return ResponseWrapper.ok("Receipt history retrieved successfully", history);
    }

    @GetMapping("/history")
    @Operation(summary = "Get all receipt history", description = "Get paginated receipt history")
    public ResponseEntity<CommonResponse<Page<ReceiptHistoryDTO>>> getReceiptHistoryPaginated(
            @RequestParam Long receiptId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /receipts/history - Fetching paginated history for receipt {}", receiptId);
        Page<ReceiptHistoryDTO> history = receiptService.getReceiptHistoryPaginated(receiptId, pageable);
        return ResponseWrapper.ok("Receipt history retrieved successfully", history);
    }

    // ==================== SUMMARY & STATS ====================

    @GetMapping("/summary")
    @Operation(summary = "Get receipt summary", description = "Get overall receipt statistics")
    public ResponseEntity<CommonResponse<ReceiptSummaryDTO>> getReceiptSummary() {
        log.info("GET /receipts/summary - Fetching receipt summary");
        ReceiptSummaryDTO summary = receiptService.getReceiptSummary();
        return ResponseWrapper.ok("Receipt summary retrieved successfully", summary);
    }

    @GetMapping("/summary/date-range")
    @Operation(summary = "Get receipt summary by date range", description = "Get receipt statistics for date range")
    public ResponseEntity<CommonResponse<ReceiptSummaryDTO>> getReceiptSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /receipts/summary/date-range - Fetching summary from {} to {}", startDate, endDate);
        ReceiptSummaryDTO summary = receiptService.getReceiptSummaryByDateRange(startDate, endDate);
        return ResponseWrapper.ok("Receipt summary retrieved successfully", summary);
    }

    @GetMapping("/case/{caseId}/count")
    @Operation(summary = "Count receipts by case", description = "Get receipt count for a case")
    public ResponseEntity<CommonResponse<Long>> countReceiptsByCaseId(@PathVariable Long caseId) {
        log.info("GET /receipts/case/{}/count - Counting receipts", caseId);
        Long count = receiptService.countReceiptsByCaseId(caseId);
        return ResponseWrapper.ok("Receipt count retrieved", count);
    }
}

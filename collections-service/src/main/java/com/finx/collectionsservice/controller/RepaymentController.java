package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.dto.payment.*;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import com.finx.collectionsservice.service.PaymentIntegrationService;
import com.finx.collectionsservice.service.RepaymentService;
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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collections/repayments")
@RequiredArgsConstructor
@Tag(name = "Repayment Management", description = "APIs for repayment tracking, approval, reconciliation, and digital payments")
public class RepaymentController {

    private final RepaymentService repaymentService;
    private final PaymentIntegrationService paymentIntegrationService;

    // ==================== Core Repayment APIs ====================

    @PostMapping
    @Operation(summary = "Capture Repayment", description = "Record a cash/cheque repayment (manual capture)")
    public ResponseEntity<CommonResponse<RepaymentDTO>> createRepayment(
            @Valid @RequestBody CreateRepaymentRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /repayments - Creating repayment for case: {}", request.getCaseId());
        RepaymentDTO response = repaymentService.createRepayment(request, userId);
        return ResponseWrapper.created("Repayment captured successfully", response);
    }

    @GetMapping("/{repaymentId}")
    @Operation(summary = "Get repayment by ID", description = "Get repayment details")
    public ResponseEntity<CommonResponse<RepaymentDTO>> getRepayment(@PathVariable Long repaymentId) {
        log.info("GET /repayments/{} - Fetching repayment", repaymentId);
        RepaymentDTO response = repaymentService.getRepayment(repaymentId);
        return ResponseWrapper.ok("Repayment retrieved successfully", response);
    }

    @GetMapping("/number/{repaymentNumber}")
    @Operation(summary = "Get repayment by number", description = "Get repayment by its number")
    public ResponseEntity<CommonResponse<RepaymentDTO>> getRepaymentByNumber(
            @PathVariable String repaymentNumber) {
        log.info("GET /repayments/number/{} - Fetching repayment", repaymentNumber);
        RepaymentDTO response = repaymentService.getRepaymentByNumber(repaymentNumber);
        return ResponseWrapper.ok("Repayment retrieved successfully", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get case repayments", description = "Get all repayments for a case")
    public ResponseEntity<CommonResponse<List<RepaymentDTO>>> getCaseRepayments(@PathVariable Long caseId) {
        log.info("GET /repayments/case/{} - Fetching case repayments", caseId);
        List<RepaymentDTO> repayments = repaymentService.getCaseRepayments(caseId);
        return ResponseWrapper.ok("Case repayments retrieved successfully", repayments);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get repayments by status", description = "Get repayments filtered by approval status")
    public ResponseEntity<CommonResponse<Page<RepaymentDTO>>> getRepaymentsByStatus(
            @PathVariable RepaymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /repayments/status/{} - Fetching repayments", status);
        Page<RepaymentDTO> repayments = repaymentService.getRepaymentsByStatus(status, pageable);
        return ResponseWrapper.ok("Repayments retrieved successfully", repayments);
    }

    @GetMapping("/search")
    @Operation(summary = "Search repayments", description = "Search repayments with filters")
    public ResponseEntity<CommonResponse<Page<RepaymentDTO>>> searchRepayments(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) RepaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /repayments/search - term: {}, status: {}", searchTerm, status);
        Page<RepaymentDTO> repayments = repaymentService.searchRepayments(searchTerm, status, fromDate, toDate, pageable);
        return ResponseWrapper.ok("Search results retrieved", repayments);
    }

    // ==================== Approval APIs (Maker-Checker) ====================

    @PostMapping("/{repaymentId}/approve")
    @Operation(summary = "Approve repayment", description = "Approve a pending repayment")
    public ResponseEntity<CommonResponse<RepaymentDTO>> approveRepayment(
            @PathVariable Long repaymentId,
            @RequestHeader("X-User-Id") Long approverId,
            @RequestParam(required = false) String comments) {
        log.info("POST /repayments/{}/approve - Approving repayment", repaymentId);
        RepaymentDTO response = repaymentService.approveRepayment(repaymentId, approverId, comments);
        return ResponseWrapper.ok("Repayment approved successfully", response);
    }

    @PostMapping("/{repaymentId}/reject")
    @Operation(summary = "Reject repayment", description = "Reject a pending repayment")
    public ResponseEntity<CommonResponse<RepaymentDTO>> rejectRepayment(
            @PathVariable Long repaymentId,
            @RequestHeader("X-User-Id") Long approverId,
            @RequestParam String reason) {
        log.info("POST /repayments/{}/reject - Rejecting repayment", repaymentId);
        RepaymentDTO response = repaymentService.rejectRepayment(repaymentId, approverId, reason);
        return ResponseWrapper.ok("Repayment rejected", response);
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Get repayments pending approval for maker-checker")
    public ResponseEntity<CommonResponse<Page<RepaymentDTO>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /repayments/pending-approvals - Fetching pending approvals");
        Page<RepaymentDTO> repayments = repaymentService.getPendingApprovals(pageable);
        return ResponseWrapper.ok("Pending approvals retrieved successfully", repayments);
    }

    // ==================== Dashboard APIs ====================

    @GetMapping("/dashboard")
    @Operation(summary = "Get repayment dashboard", description = "Get repayment dashboard statistics")
    public ResponseEntity<CommonResponse<RepaymentDashboardDTO>> getDashboard() {
        log.info("GET /repayments/dashboard - Fetching dashboard stats");
        RepaymentDashboardDTO dashboard = repaymentService.getDashboardStats();
        return ResponseWrapper.ok("Dashboard statistics retrieved", dashboard);
    }

    @GetMapping("/sla-dashboard")
    @Operation(summary = "Get SLA dashboard", description = "Get SLA monitoring dashboard statistics")
    public ResponseEntity<CommonResponse<SlaDashboardDTO>> getSlaDashboard() {
        log.info("GET /repayments/sla-dashboard - Fetching SLA dashboard stats");
        SlaDashboardDTO slaDashboard = repaymentService.getSlaDashboardStats();
        return ResponseWrapper.ok("SLA dashboard statistics retrieved", slaDashboard);
    }

    @GetMapping("/sla-breached")
    @Operation(summary = "Get SLA breached repayments", description = "Get repayments with SLA breaches (Exception Queue)")
    public ResponseEntity<CommonResponse<List<RepaymentDTO>>> getSlaBreachedRepayments() {
        log.info("GET /repayments/sla-breached - Fetching SLA breached repayments");
        List<RepaymentDTO> repayments = repaymentService.getSlaBreachedRepayments();
        return ResponseWrapper.ok("SLA breached repayments retrieved successfully", repayments);
    }

    // ==================== Reconciliation APIs ====================

    @GetMapping("/reconciliation/pending")
    @Operation(summary = "Get pending reconciliation", description = "Get repayments pending reconciliation")
    public ResponseEntity<CommonResponse<Page<ReconciliationDTO>>> getPendingReconciliation(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /repayments/reconciliation/pending - Fetching pending reconciliation");
        Page<ReconciliationDTO> pending = repaymentService.getPendingReconciliation(pageable);
        return ResponseWrapper.ok("Pending reconciliation retrieved", pending);
    }

    @PostMapping("/reconciliation/update")
    @Operation(summary = "Update reconciliation status", description = "Update reconciliation status for a repayment")
    public ResponseEntity<CommonResponse<ReconciliationDTO>> updateReconciliation(
            @Valid @RequestBody ReconciliationUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /repayments/reconciliation/update - Updating reconciliation for: {}", request.getRepaymentId());
        ReconciliationDTO response = repaymentService.updateReconciliationStatus(request, userId);
        return ResponseWrapper.ok("Reconciliation status updated", response);
    }

    @PostMapping("/reconciliation/bulk")
    @Operation(summary = "Bulk reconcile", description = "Bulk reconcile multiple repayments")
    public ResponseEntity<CommonResponse<List<ReconciliationDTO>>> bulkReconcile(
            @Valid @RequestBody BulkReconciliationRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /repayments/reconciliation/bulk - Bulk reconciling {} repayments", request.getRepaymentIds().size());
        List<ReconciliationDTO> response = repaymentService.bulkReconcile(request, userId);
        return ResponseWrapper.ok("Bulk reconciliation completed", response);
    }

    // ==================== Partial Payment APIs ====================

    @PostMapping("/partial-payment")
    @Operation(summary = "Record partial payment", description = "Record a partial payment adjustment")
    public ResponseEntity<CommonResponse<RepaymentDTO>> recordPartialPayment(
            @Valid @RequestBody PartialPaymentRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /repayments/partial-payment - Recording partial payment for case: {}", request.getCaseId());
        RepaymentDTO response = repaymentService.recordPartialPayment(request, userId);
        return ResponseWrapper.created("Partial payment recorded", response);
    }

    // ==================== Receipt APIs ====================

    @GetMapping("/{repaymentId}/receipt")
    @Operation(summary = "Download receipt", description = "Download receipt PDF for any repayment")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long repaymentId) {
        log.info("GET /repayments/{}/receipt - Downloading receipt PDF", repaymentId);
        byte[] receipt = repaymentService.generateReceipt(repaymentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + repaymentId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(receipt);
    }

    @GetMapping("/{repaymentId}/receipt/details")
    @Operation(summary = "Get receipt details", description = "Get receipt details/metadata for a repayment")
    public ResponseEntity<CommonResponse<RepaymentDTO>> getReceiptDetails(@PathVariable Long repaymentId) {
        log.info("GET /repayments/{}/receipt/details - Getting receipt details", repaymentId);
        RepaymentDTO response = repaymentService.getReceiptDetails(repaymentId);
        return ResponseWrapper.ok("Receipt details retrieved", response);
    }

    // ==================== Digital Payment APIs ====================
    // Service types: DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL

    @PostMapping("/payments/initiate")
    @Operation(summary = "Initiate Payment",
               description = "Initiate digital payment. Types: DYNAMIC_QR (amount), PAYMENT_LINK (amount, mobileNumber), COLLECT_CALL (amount, instrumentType, instrumentReference)")
    public ResponseEntity<CommonResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitRequest request) {
        log.info("POST /repayments/payments/initiate - Type: {}, Amount: {}", request.getServiceType(), request.getAmount());
        PaymentResponse response = paymentIntegrationService.initiatePayment(request);
        return ResponseWrapper.created("Payment initiated successfully", response);
    }

    @PostMapping("/payments/status")
    @Operation(summary = "Check Payment Status", description = "Check status of a payment transaction")
    public ResponseEntity<CommonResponse<PaymentResponse>> checkPaymentStatus(
            @Valid @RequestBody PaymentStatusRequest request) {
        log.info("POST /repayments/payments/status - Type: {}, TxnId: {}", request.getServiceType(), request.getTransactionId());
        PaymentResponse response = paymentIntegrationService.checkStatus(request);
        return ResponseWrapper.ok("Payment status retrieved", response);
    }

    @PostMapping("/payments/cancel")
    @Operation(summary = "Cancel Payment", description = "Cancel a pending payment transaction")
    public ResponseEntity<CommonResponse<PaymentResponse>> cancelPayment(
            @Valid @RequestBody PaymentCancelRequest request) {
        log.info("POST /repayments/payments/cancel - Type: {}, TxnId: {}", request.getServiceType(), request.getTransactionId());
        PaymentResponse response = paymentIntegrationService.cancelPayment(request);
        return ResponseWrapper.ok("Payment cancelled successfully", response);
    }

    @PostMapping("/payments/refund")
    @Operation(summary = "Refund Payment", description = "Refund a completed payment. If amount not provided, full refund.")
    public ResponseEntity<CommonResponse<PaymentResponse>> refundPayment(
            @Valid @RequestBody PaymentRefundRequest request) {
        log.info("POST /repayments/payments/refund - Type: {}, TxnId: {}", request.getServiceType(), request.getTransactionId());
        PaymentResponse response = paymentIntegrationService.refundPayment(request);
        return ResponseWrapper.ok("Refund processed successfully", response);
    }

    @GetMapping("/payments/transaction/{transactionId}")
    @Operation(summary = "Get Payment Transaction", description = "Get payment transaction details")
    public ResponseEntity<CommonResponse<PaymentResponse>> getPaymentTransaction(
            @PathVariable String transactionId) {
        log.info("GET /repayments/payments/transaction/{}", transactionId);
        PaymentResponse response = paymentIntegrationService.getTransaction(transactionId);
        return ResponseWrapper.ok("Transaction retrieved", response);
    }

    @GetMapping("/payments/case/{caseId}")
    @Operation(summary = "Get Payment Transactions by Case", description = "Get all payment transactions for a case")
    public ResponseEntity<CommonResponse<List<PaymentResponse>>> getPaymentTransactionsByCase(
            @PathVariable Long caseId) {
        log.info("GET /repayments/payments/case/{}", caseId);
        List<PaymentResponse> response = paymentIntegrationService.getTransactionsByCase(caseId);
        return ResponseWrapper.ok("Transactions retrieved", response);
    }

    @PostMapping("/payments/{transactionId}/generate-receipt")
    @Operation(summary = "Generate Receipt for Payment",
               description = "Generate receipt for a successful payment transaction")
    public ResponseEntity<CommonResponse<RepaymentDTO>> generateReceiptForPayment(
            @PathVariable String transactionId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /repayments/payments/{}/generate-receipt - Generating receipt", transactionId);
        RepaymentDTO response = paymentIntegrationService.generateReceiptForTransaction(transactionId, userId);
        return ResponseWrapper.created("Receipt generated successfully", response);
    }

    @GetMapping("/payments/{transactionId}/receipt/download")
    @Operation(summary = "Download Receipt for Payment",
               description = "Download receipt PDF for a successful payment transaction")
    public ResponseEntity<byte[]> downloadReceiptForPayment(
            @PathVariable String transactionId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /repayments/payments/{}/receipt/download - Downloading receipt", transactionId);
        byte[] receipt = paymentIntegrationService.downloadReceiptForTransaction(transactionId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + transactionId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(receipt);
    }
}

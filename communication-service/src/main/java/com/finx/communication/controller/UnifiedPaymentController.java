package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.domain.dto.payment.*;
import com.finx.communication.service.payment.UnifiedPaymentService;
import com.finx.communication.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified Payment Controller - 4 APIs for all payment types
 * Service types: DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Unified Payment", description = "Single API for all payment types: DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL")
public class UnifiedPaymentController {

    private final UnifiedPaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate Payment",
               description = "Initiate payment based on serviceType. " +
                       "DYNAMIC_QR: amount required. " +
                       "PAYMENT_LINK: amount, mobileNumber required. " +
                       "COLLECT_CALL: amount, instrumentType, instrumentReference required.")
    public ResponseEntity<CommonResponse<UnifiedPaymentResponse>> initiate(
            @Valid @RequestBody UnifiedPaymentRequest request) {
        log.info("POST /payments/initiate - Type: {}, Amount: {}", request.getServiceType(), request.getAmount());
        UnifiedPaymentResponse response = paymentService.initiate(request);
        return ResponseWrapper.created("Payment initiated successfully", response);
    }

    @PostMapping("/status")
    @Operation(summary = "Check Payment Status",
               description = "Check status of a payment transaction")
    public ResponseEntity<CommonResponse<UnifiedPaymentResponse>> status(
            @Valid @RequestBody UnifiedStatusRequest request) {
        log.info("POST /payments/status - Type: {}, TxnId: {}", request.getServiceType(), request.getTransactionId());
        UnifiedPaymentResponse response = paymentService.status(request);
        return ResponseWrapper.ok("Payment status retrieved", response);
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel Payment",
               description = "Cancel a pending payment transaction")
    public ResponseEntity<CommonResponse<UnifiedPaymentResponse>> cancel(
            @Valid @RequestBody UnifiedCancelRequest request) {
        log.info("POST /payments/cancel - Type: {}, TxnId: {}", request.getServiceType(), request.getTransactionId());
        UnifiedPaymentResponse response = paymentService.cancel(request);
        return ResponseWrapper.ok("Payment cancelled successfully", response);
    }

    @PostMapping("/refund")
    @Operation(summary = "Refund Payment",
               description = "Refund a completed payment. If amount not provided, full refund.")
    public ResponseEntity<CommonResponse<UnifiedPaymentResponse>> refund(
            @Valid @RequestBody UnifiedRefundRequest request) {
        log.info("POST /payments/refund - Type: {}, TxnId: {}", request.getServiceType(), request.getTransactionId());
        UnifiedPaymentResponse response = paymentService.refund(request);
        return ResponseWrapper.ok("Refund processed successfully", response);
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get Transaction", description = "Get transaction details by ID")
    public ResponseEntity<CommonResponse<UnifiedPaymentResponse>> getTransaction(
            @PathVariable String transactionId) {
        log.info("GET /payments/transaction/{}", transactionId);
        UnifiedPaymentResponse response = paymentService.getTransaction(transactionId);
        return ResponseWrapper.ok("Transaction retrieved", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get Transactions by Case", description = "Get all transactions for a case")
    public ResponseEntity<CommonResponse<List<UnifiedPaymentResponse>>> getByCase(
            @PathVariable Long caseId) {
        log.info("GET /payments/case/{}", caseId);
        List<UnifiedPaymentResponse> response = paymentService.getTransactionsByCase(caseId);
        return ResponseWrapper.ok("Transactions retrieved", response);
    }
}

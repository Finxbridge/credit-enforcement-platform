package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.payment.*;
import com.finx.communication.service.payment.PaymentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/integrations/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Gateway", description = "Payment gateway integration APIs for Payment Link generation, status check, and refunds")
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/link")
    @Operation(summary = "Generate Payment Link",
               description = "Generate a payment link for repayment collection. Only amount and mobileNumber are required; other fields are fetched from third_party_integration_master.")
    public ResponseEntity<CommonResponse<PaymentResponse>> generatePaymentLink(
            @Valid @RequestBody PaymentInitiateRequest request) {
        log.info("Request to generate payment link for amount: {} to mobile: {}",
                request.getAmount(), request.getMobileNumber());
        PaymentResponse response = paymentGatewayService.generatePaymentLink(request);
        return ResponseWrapper.created("Payment link generated successfully", response);
    }

    @PostMapping("/status")
    @Operation(summary = "Check Payment Status",
               description = "Check the status of a payment transaction using transaction ID")
    public ResponseEntity<CommonResponse<PaymentResponse>> checkPaymentStatus(
            @Valid @RequestBody PaymentStatusRequest request) {
        log.info("Request to check payment status for transaction: {}", request.getTransactionId());
        PaymentResponse response = paymentGatewayService.checkPaymentStatus(request);
        return ResponseWrapper.ok("Payment status retrieved successfully", response);
    }

    @PostMapping("/refund")
    @Operation(summary = "Process Refund",
               description = "Process a refund for a completed payment. Only transactionId is required; if amount is not provided, full refund will be processed.")
    public ResponseEntity<CommonResponse<PaymentResponse>> processRefund(
            @Valid @RequestBody PaymentRefundRequest request) {
        log.info("Request to process refund for transaction: {}", request.getTransactionId());
        PaymentResponse response = paymentGatewayService.processRefund(request);
        return ResponseWrapper.ok("Refund processed successfully", response);
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get Transaction Details",
               description = "Get complete transaction details from database by transaction ID")
    public ResponseEntity<CommonResponse<PaymentResponse>> getTransactionDetails(
            @PathVariable String transactionId) {
        log.info("Request to get transaction details for: {}", transactionId);
        PaymentResponse response = paymentGatewayService.getTransactionDetails(transactionId);
        return ResponseWrapper.ok("Transaction details retrieved", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get Transactions by Case",
               description = "Get all payment transactions for a specific case")
    public ResponseEntity<CommonResponse<java.util.List<PaymentResponse>>> getTransactionsByCase(
            @PathVariable Long caseId) {
        log.info("Request to get transactions for case: {}", caseId);
        java.util.List<PaymentResponse> response = paymentGatewayService.getTransactionsByCase(caseId);
        return ResponseWrapper.ok("Transactions retrieved", response);
    }
}

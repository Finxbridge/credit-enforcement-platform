package com.finx.communication.controller;

import com.finx.common.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
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
@Tag(name = "Payment Gateway", description = "Payment gateway integration APIs (PhonePe/Razorpay)")
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate Payment", description = "Initiate payment/generate payment link")
    public ResponseEntity<CommonResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request) {
        log.info("Request to initiate payment for amount: {}", request.getAmount());
        PaymentResponse response = paymentGatewayService.initiatePayment(request);
        return ResponseWrapper.ok("Payment initiated successfully", response);
    }

    @GetMapping("/status/{transactionId}")
    @Operation(summary = "Get Payment Status", description = "Check payment status")
    public ResponseEntity<CommonResponse<PaymentResponse>> getPaymentStatus(@PathVariable String transactionId) {
        log.info("Request to get payment status for: {}", transactionId);
        PaymentResponse response = paymentGatewayService.getPaymentStatus(transactionId);
        return ResponseWrapper.ok("Payment status retrieved", response);
    }
}

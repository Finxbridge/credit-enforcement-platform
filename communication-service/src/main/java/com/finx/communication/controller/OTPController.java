package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.otp.*;
import com.finx.communication.service.communication.OTPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OTP Controller - MSG91 Integration
 * Returns standardized CommonResponse with MSG91 API responses in payload
 *
 * Endpoints:
 * - POST /send - Send OTP
 * - GET /verify - Verify OTP
 * - GET /resend - Resend OTP
 * - GET /analytics - Get OTP Analytics
 *
 * @version 2.0.0 - Updated to use CommonResponse wrapper
 */
@Slf4j
@RestController
@RequestMapping("/comm/otp")
@RequiredArgsConstructor
@Tag(name = "OTP Service", description = "MSG91 OTP APIs - Wrapped in standard response format")
public class OTPController {

    private final OTPService otpService;

    @PostMapping("/send")
    @Operation(summary = "Send OTP", description = "Send OTP via MSG91. Returns wrapped MSG91 response in payload.")
    public ResponseEntity<CommonResponse<Map<String, Object>>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        log.info("Request to send OTP to: {}", request.getMobile());
        Map<String, Object> response = otpService.sendOtp(request);
        return ResponseWrapper.ok("OTP sent successfully", response);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP code. Returns wrapped MSG91 response in payload.")
    public ResponseEntity<CommonResponse<Map<String, Object>>> verifyOtp(
            @RequestParam String otp,
            @RequestParam String mobile) {
        log.info("Request to verify OTP for: {}", mobile);
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .otp(otp)
                .mobile(mobile)
                .build();
        Map<String, Object> response = otpService.verifyOtp(request);
        return ResponseWrapper.ok("OTP verified successfully", response);
    }

    @GetMapping("/resend")
    @Operation(summary = "Resend OTP", description = "Resend OTP to mobile number. Returns wrapped MSG91 response in payload.")
    public ResponseEntity<CommonResponse<Map<String, Object>>> resendOtp(@RequestParam String mobile) {
        log.info("Request to resend OTP to: {}", mobile);
        Map<String, Object> response = otpService.resendOtp(mobile);
        return ResponseWrapper.ok("OTP resent successfully", response);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get OTP Analytics", description = "Get OTP analytics by date range. Returns wrapped analytics data in payload.")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getAnalytics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Request for OTP analytics from {} to {}", startDate, endDate);
        Map<String, Object> analytics = otpService.getAnalytics(startDate, endDate);
        return ResponseWrapper.ok("OTP analytics retrieved successfully", analytics);
    }
}

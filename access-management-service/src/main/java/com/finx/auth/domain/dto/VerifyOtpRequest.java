package com.finx.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Verify OTP Request DTO
 * Purpose: Verify OTP code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRequest {

    @NotBlank(message = "Request ID is required")
    private String requestId; // OTP request ID from request-otp response

    @NotBlank(message = "OTP code is required")
    private String otpCode; // 6-digit OTP

    @NotBlank(message = "Email is required")
    private String username; // Email/username
}

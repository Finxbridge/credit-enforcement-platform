package com.finx.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resend OTP Request DTO
 * Purpose: Resend OTP to user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendOtpRequest {

    @NotBlank(message = "Request ID is required")
    private String requestId; // Previous OTP request ID

    @NotBlank(message = "Email is required")
    private String username; // Email/username
}

package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request OTP Response DTO
 * Purpose: Response for OTP request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestOtpResponse {

    private String requestId; // OTP request ID for verification
    private String message;
    private String email; // Masked email (e.g., u***@example.com)
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    private Integer remainingAttempts;
}

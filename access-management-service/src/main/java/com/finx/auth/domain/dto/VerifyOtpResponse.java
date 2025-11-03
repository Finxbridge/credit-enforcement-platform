package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Verify OTP Response DTO
 * Purpose: Response for OTP verification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpResponse {

    private Boolean verified;
    private String message;
    private String resetToken; // Token to be used for password reset (valid for short duration)
}

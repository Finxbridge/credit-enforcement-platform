package com.finx.auth.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request OTP Request DTO
 * Purpose: Request OTP for password reset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String username; // Email address
    private String purpose; // RESET_PASSWORD, LOGIN, etc.
}

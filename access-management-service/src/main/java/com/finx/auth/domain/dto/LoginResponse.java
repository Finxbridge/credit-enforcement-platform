package com.finx.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 * Purpose: Response for login endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    private String role; // ✅ PRIMARY ROLE CODE (SUPER_ADMIN, ADMIN, SUPERVISOR, AGENT)

    private Boolean isFirstLogin; // True if user needs to change password
    private Boolean requiresOtp; // True if OTP is required for password reset

    private String message; // Additional message (e.g., "Please reset your password")

    // Token details
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresAt;
    private Long refreshExpiresAt;
    private String sessionId;
}

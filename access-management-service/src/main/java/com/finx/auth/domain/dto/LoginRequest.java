package com.finx.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * Purpose: User login with username/password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username; // Can be email or username

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceType; // WEB, MOBILE, TABLET
    private String ipAddress;
    private String userAgent;
}

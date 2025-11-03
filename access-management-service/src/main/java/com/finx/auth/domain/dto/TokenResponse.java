package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token Response DTO
 * Purpose: JWT token response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType; // Bearer
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private String sessionId; // Session identifier
}

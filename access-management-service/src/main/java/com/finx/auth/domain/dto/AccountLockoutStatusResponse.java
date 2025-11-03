package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Account Lockout Status Response DTO
 * Purpose: Check account lockout status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLockoutStatusResponse {

    private Boolean isLocked;
    private Integer failedAttempts;
    private LocalDateTime lockedUntil;
    private String message;
}

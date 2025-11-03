package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reset Password Response DTO
 * Purpose: Response for password reset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordResponse {

    private Boolean success;
    private String message;
}

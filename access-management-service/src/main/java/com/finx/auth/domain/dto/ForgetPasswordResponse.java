package com.finx.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forget Password Response DTO
 * Purpose: Response for forget password request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgetPasswordResponse {

    private String requestId;
    private String email;
    private String message;
    private Boolean otpSent;
    private Integer otpExpiryMinutes;
}

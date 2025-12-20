package com.finx.auth.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forget Password Request DTO
 * Purpose: Request to initiate forget password flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}

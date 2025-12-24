package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.PolicyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePasswordPolicyRequest {

    private Long organizationId;

    @NotBlank(message = "Policy code is required")
    @Size(max = 50, message = "Policy code must be at most 50 characters")
    private String policyCode;

    @NotBlank(message = "Policy name is required")
    @Size(max = 200, message = "Policy name must be at most 200 characters")
    private String policyName;

    @NotNull(message = "Policy level is required")
    private PolicyLevel policyLevel;

    private Integer minLength;
    private Integer maxLength;
    private Boolean requireUppercase;
    private Boolean requireLowercase;
    private Boolean requireNumber;
    private Boolean requireSpecialChar;
    private String specialCharsAllowed;
    private Integer passwordHistoryCount;
    private Integer preventReuseDays;
    private Integer passwordExpiryDays;
    private Integer warnBeforeExpiryDays;
    private Integer maxFailedAttempts;
    private Integer lockoutDurationMinutes;
    private Boolean isDefault;
}

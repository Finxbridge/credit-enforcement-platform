package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.PolicyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPolicyDTO {
    private Long id;
    private Long organizationId;
    private String policyCode;
    private String policyName;
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
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

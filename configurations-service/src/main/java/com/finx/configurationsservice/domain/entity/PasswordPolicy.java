package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.PolicyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "policy_code", unique = true, nullable = false, length = 50)
    private String policyCode;

    @Column(name = "policy_name", nullable = false, length = 200)
    private String policyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_level", nullable = false, length = 20)
    private PolicyLevel policyLevel;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "require_uppercase")
    private Boolean requireUppercase;

    @Column(name = "require_lowercase")
    private Boolean requireLowercase;

    @Column(name = "require_number")
    private Boolean requireNumber;

    @Column(name = "require_special_char")
    private Boolean requireSpecialChar;

    @Column(name = "special_chars_allowed", length = 100)
    private String specialCharsAllowed;

    @Column(name = "password_history_count")
    private Integer passwordHistoryCount;

    @Column(name = "prevent_reuse_days")
    private Integer preventReuseDays;

    @Column(name = "password_expiry_days")
    private Integer passwordExpiryDays;

    @Column(name = "warn_before_expiry_days")
    private Integer warnBeforeExpiryDays;

    @Column(name = "max_failed_attempts")
    private Integer maxFailedAttempts;

    @Column(name = "lockout_duration_minutes")
    private Integer lockoutDurationMinutes;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (minLength == null) {
            minLength = 8;
        }
        if (maxLength == null) {
            maxLength = 128;
        }
        if (requireUppercase == null) {
            requireUppercase = true;
        }
        if (requireLowercase == null) {
            requireLowercase = true;
        }
        if (requireNumber == null) {
            requireNumber = true;
        }
        if (requireSpecialChar == null) {
            requireSpecialChar = true;
        }
        if (passwordHistoryCount == null) {
            passwordHistoryCount = 5;
        }
        if (passwordExpiryDays == null) {
            passwordExpiryDays = 90;
        }
        if (maxFailedAttempts == null) {
            maxFailedAttempts = 5;
        }
        if (lockoutDurationMinutes == null) {
            lockoutDurationMinutes = 30;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.finx.auth.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * User Entity
 * Purpose: User accounts with comprehensive security features and
 * auto-allocation configuration
 */
@Entity(name = "AuthUser")
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, LOCKED, SUSPENDED

    @Column(name = "user_group_id")
    private Long userGroupId;

    // AUTO-ALLOCATION FIELDS
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assigned_geographies", columnDefinition = "jsonb")
    private String assignedGeographies; // JSON array of geography codes

    @Column(name = "max_case_capacity")
    @Builder.Default
    private Integer maxCaseCapacity = 100;

    @Column(name = "current_case_count")
    @Builder.Default
    private Integer currentCaseCount = 0;

    @Column(name = "allocation_percentage", columnDefinition = "DECIMAL(5,2)")
    @Builder.Default
    private Double allocationPercentage = 100.00;

    @Column(name = "allocation_bucket", length = 50)
    private String allocationBucket;

    @Column(name = "team_id")
    private Long teamId;

    // SECURITY FIELDS
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "session_expires_at")
    private LocalDateTime sessionExpiresAt;

    @Column(name = "is_first_login")
    @Builder.Default
    private Boolean isFirstLogin = false; // True for first login

    @Column(name = "otp_secret", length = 255)
    private String otpSecret;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    // AUDIT FIELDS
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isFirstLogin() {
        return Boolean.TRUE.equals(isFirstLogin);
    }

    public void setFirstLogin(boolean isFirstLogin) {
        this.isFirstLogin = isFirstLogin;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    public void lockAccount(int lockoutDurationMinutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
    }
}

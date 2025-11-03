package com.finx.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking OTP requests
 * Shared entity used by access-management-service and communication-service
 */
@Entity
@Table(name = "otp_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true, nullable = false, length = 100)
    private String requestId;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // SMS, WHATSAPP, EMAIL

    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose; // LOGIN, RESET_PASSWORD, TRANSACTION

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SENT, VERIFIED, EXPIRED, FAILED

    @Builder.Default
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    @Builder.Default
    @Column(name = "max_attempts")
    private Integer maxAttempts = 3;

    @Builder.Default
    @Column(name = "provider", length = 50)
    private String provider = "MSG91";

    @Column(name = "provider_request_id", length = 100)
    private String providerRequestId;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(5); // Default 5 min expiry
        }
    }

    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if max attempts reached
     */
    public boolean isMaxAttemptsReached() {
        return attemptCount >= maxAttempts;
    }

    /**
     * Increment attempt count
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }
}

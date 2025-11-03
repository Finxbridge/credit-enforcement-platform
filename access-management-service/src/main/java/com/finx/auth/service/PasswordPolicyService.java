package com.finx.auth.service;

import com.finx.common.constants.CacheConstants;
import com.finx.common.service.ConfigCacheService;
import com.finx.auth.domain.entity.User;
import com.finx.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * PasswordPolicyService
 * Purpose: Password validation and account lockout management
 * Configuration: Loads from system_config table via ConfigCacheService
 * FR: FR-AM-3, FR-AM-6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordPolicyService {

    private final AuthUserRepository authUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfigCacheService configCacheService;

    // Configuration Keys

    // Defaults
    private static final int DEFAULT_MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int DEFAULT_ACCOUNT_LOCKOUT_DURATION = 30;

    // Password complexity regex: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1
    // special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    /**
     * Validate password strength
     * FR-AM-3: Password policy enforcement
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Get password validation error message
     */
    public String getPasswordValidationMessage() {
        return "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                "one lowercase letter, one digit, and one special character (@$!%*?&)";
    }

    /**
     * Hash password using BCrypt
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verify password against hash
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        try {
            if (isBCryptHash(hashedPassword)) {
                return passwordEncoder.matches(rawPassword, hashedPassword);
            } else {
                return passwordEncoder.matches(rawPassword, passwordEncoder.encode(hashedPassword));
            }
        } catch (Exception e) {
            log.error("Error verifying password: {}", e.getMessage());
            return false;
        }
    }

    private boolean isBCryptHash(String password) {
        try {
            return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Handle failed login attempt
     * FR-AM-6: Account lockout after configured failed attempts
     *
     * @param user User entity
     * @return Remaining login attempts before lockout (0 if account is now locked)
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int handleFailedLogin(User user) {
        int maxAttempts = configCacheService.getIntConfig(CacheConstants.SECURITY_MAX_FAILED_LOGIN_ATTEMPTS,
                DEFAULT_MAX_FAILED_LOGIN_ATTEMPTS);

        user.incrementFailedLoginAttempts();

        log.warn("Failed login attempt for user: {}. Attempt count: {}",
                user.getUsername(), user.getFailedLoginAttempts());

        // Save the user immediately to persist the failed attempt count
        authUserRepository.save(user);

        int remainingAttempts = maxAttempts - user.getFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= maxAttempts) {
            lockAccount(user);
            log.warn("Account locked for user: {} after {} failed attempts",
                    user.getUsername(), maxAttempts);
            remainingAttempts = 0;
        }

        return remainingAttempts;
    }

    /**
     * Lock user account
     */
    @Transactional
    public void lockAccount(User user) {
        int lockoutDuration = configCacheService.getIntConfig(CacheConstants.SECURITY_ACCOUNT_LOCKOUT_DURATION_MINUTES,
                DEFAULT_ACCOUNT_LOCKOUT_DURATION);
        user.lockAccount(lockoutDuration);
        user.setStatus("LOCKED");
        authUserRepository.save(user);
    }

    /**
     * Unlock user account (Admin action)
     */
    @Transactional
    public void unlockAccount(User user) {
        user.resetFailedLoginAttempts();
        user.setStatus("ACTIVE");
        authUserRepository.save(user);
        log.info("Account unlocked for user: {}", user.getUsername());
    }

    /**
     * Reset failed login attempts after successful login
     */
    @Transactional
    public void resetFailedLoginAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
            authUserRepository.save(user);
        }
    }

    /**
     * Check if account is locked
     */
    public boolean isAccountLocked(User user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }

        // Check if lockout period has expired
        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            // Auto-unlock expired lockout
            unlockAccount(user);
            return false;
        }

        return true;
    }

    /**
     * Get remaining lockout time in minutes
     */
    public long getRemainingLockoutMinutes(User user) {
        if (user.getAccountLockedUntil() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(user.getAccountLockedUntil())) {
            return 0;
        }

        return java.time.Duration.between(now, user.getAccountLockedUntil()).toMinutes();
    }

    /**
     * Validate password confirmation match
     */
    public boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}
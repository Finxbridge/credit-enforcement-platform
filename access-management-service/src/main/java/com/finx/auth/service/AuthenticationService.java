package com.finx.auth.service;

import com.finx.common.constants.CacheConstants;
import com.finx.common.exception.BusinessException;
import com.finx.common.exception.ResourceNotFoundException;
import com.finx.common.exception.UnauthorizedException;
import com.finx.common.domain.dto.InternalEmailRequest;
import com.finx.common.service.ConfigCacheService;
import com.finx.common.model.OtpRequest;
import com.finx.auth.client.CommunicationServiceClient;
import com.finx.auth.domain.dto.*;
import com.finx.auth.domain.entity.*;
import com.finx.auth.repository.*;
import com.finx.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AuthenticationService
 * Purpose: Core authentication logic for login, OTP, password reset, and token
 * management
 * Configuration: Loads from system_config table via ConfigCacheService
 * FR: FR-AM-1, FR-AM-2, FR-AM-3, FR-AM-4, FR-AM-5, FR-AM-6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthUserRepository authUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthRoleRepository authRoleRepository;
    private final AuthPermissionRepository authPermissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final OtpRequestRepository otpRequestRepository;
    private final PasswordPolicyService passwordPolicyService;
    private final SessionManagementService sessionManagementService;
    private final JwtUtil jwtUtil;
    private final CommunicationServiceClient communicationServiceClient;
    private final ConfigCacheService configCacheService;

    // Configuration Keys

    private static final String USER_NOT_FOUND = "User not found";

    // Defaults
    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final int DEFAULT_OTP_EXPIRY_MINUTES = 5;
    private static final int DEFAULT_OTP_MAX_ATTEMPTS = 3;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * User Login
     * FR-AM-1: First-time login detection
     * FR-AM-2: Existing user login
     * FR-AM-6: Account lockout enforcement
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        // Find user by username or email
        User user = authUserRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Check if account is locked
        if (passwordPolicyService.isAccountLocked(user)) {
            long remainingMinutes = passwordPolicyService.getRemainingLockoutMinutes(user);
            throw new UnauthorizedException(
                    "Account is locked. Please try again after " + remainingMinutes + " minutes.");
        }

        // Verify password
        if (!passwordPolicyService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            int remainingAttempts = passwordPolicyService.handleFailedLogin(user);

            if (remainingAttempts > 0) {
                throw new UnauthorizedException("Invalid credentials. " + remainingAttempts + " attempts remaining.");
            } else {
                long lockoutMinutes = passwordPolicyService.getRemainingLockoutMinutes(user);
                throw new UnauthorizedException(
                        "Account locked due to too many failed attempts. Please try again after " +
                                lockoutMinutes + " minutes.");
            }
        }

        // Reset failed login attempts on successful login
        passwordPolicyService.resetFailedLoginAttempts(user);

        // FR-AM-1: Check if first login (must change password)
        if (user.isFirstLogin()) {
            return LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .isFirstLogin(true)
                    .requiresOtp(true)
                    .message("Please reset your password using OTP verification")
                    .build();
        }

        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        authUserRepository.save(user);

        log.info("Successful login for user: {}", user.getUsername());

        // Get user's roles
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        List<Role> roles = authRoleRepository.findAllById(roleIds);
        List<String> roleCodes = roles.stream().map(Role::getRoleCode).toList();
        List<String> permissionCodes = getUserPermissions(user.getId()).getPermissions().stream()
                .map(PermissionDTO::getPermissionCode)
                .collect(Collectors.toList());

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(), roleCodes,
                permissionCodes);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // Create session
        UserSession session = sessionManagementService.createSession(
                user,
                accessToken,
                refreshToken,
                request.getIpAddress(),
                request.getUserAgent(),
                request.getDeviceType());

        // Get primary role code for user
        String primaryRole = "AGENT"; // Default fallback
        if (!roles.isEmpty()) {
            primaryRole = roles.get(0).getRoleCode();
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(primaryRole)
                .isFirstLogin(false)
                .message("Login successful.")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(jwtUtil.getAccessTokenExpirationTime()
                        .atZone(ZoneId.systemDefault())
                        .format(formatter))
                .refreshExpiresAt(jwtUtil.getRefreshTokenExpirationTime()
                        .atZone(ZoneId.systemDefault())
                        .format(formatter))
                .sessionId(session.getSessionId())
                .build();
    }

    /**
     * Request OTP for password reset
     * FR-AM-1: OTP generation and email delivery
     */
    @Transactional
    public RequestOtpResponse requestOtp(RequestOtpRequest request) {
        log.info("OTP request for user: {}", request.getUsername());

        int otpExpiryMinutes = configCacheService.getIntConfig(CacheConstants.OTP_EXPIRY_MINUTES,
                DEFAULT_OTP_EXPIRY_MINUTES);
        int otpMaxAttempts = configCacheService.getIntConfig(CacheConstants.OTP_MAX_ATTEMPTS, DEFAULT_OTP_MAX_ATTEMPTS);

        // Find user
        User user = authUserRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // Find existing active OTP request
        Optional<OtpRequest> existingOtpRequest = otpRequestRepository.findByUserIdAndPurposeAndStatus(user.getId(),
                "RESET_PASSWORD", "PENDING");

        OtpRequest otpRequest;
        if (existingOtpRequest.isPresent()) {
            otpRequest = existingOtpRequest.get();
            // Reset attempt count for verification when a new OTP is requested (e.g.,
            // resend)
            otpRequest.setAttemptCount(0);
            otpRequest.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
            otpRequest.setStatus("PENDING"); // Reset status to PENDING for new OTP
        } else {
            // Create new OTP request
            String requestId = "OTP-" + UUID.randomUUID().toString();
            otpRequest = OtpRequest
                    .builder()
                    .requestId(requestId)
                    .mobile(user.getMobileNumber())
                    .email(user.getEmail())
                    .channel("EMAIL")
                    .purpose(request.getPurpose() != null ? request.getPurpose() : "RESET_PASSWORD")
                    .status("PENDING")
                    .attemptCount(0) // Initialize attempt count to 0 for new OTP
                    .maxAttempts(otpMaxAttempts)
                    .provider("MSG91")
                    .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                    .userId(user.getId())
                    .build();
        }

        // Generate OTP
        String otpCode = generateOtp();
        String otpHash = passwordPolicyService.hashPassword(otpCode);
        otpRequest.setOtpHash(otpHash);

        otpRequestRepository.save(otpRequest);

        // Send OTP via email asynchronously (non-blocking)
        sendOtpEmailAsync(user.getEmail(), user.getFirstName(), otpCode, otpRequest.getId());

        log.info("OTP send request queued for: {}", maskEmail(user.getEmail()));

        return RequestOtpResponse.builder()
                .requestId(otpRequest.getRequestId())
                .message("OTP sent successfully to your registered email")
                .email(maskEmail(user.getEmail()))
                .expiresAt(otpRequest.getExpiresAt())
                .remainingAttempts(otpRequest.getMaxAttempts() - otpRequest.getAttemptCount())
                .build();
    }

    /**
     * Verify OTP
     * FR-AM-1: OTP verification with attempt limiting
     */
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        log.info("OTP verification for request: {}", request.getRequestId());

        // Find OTP request
        OtpRequest otpRequest = otpRequestRepository
                .findByRequestId(request.getRequestId())
                .orElseThrow(() -> new BusinessException("Invalid OTP request"));

        User user = authUserRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // Check if OTP is expired
        if (otpRequest.isExpired()) {
            throw new BusinessException("OTP has expired. Please request a new one.");
        }

        // Check if max attempts reached
        if (otpRequest.isMaxAttemptsReached()) {
            // Lock account after max OTP verification failures
            passwordPolicyService.lockAccount(user);
            throw new BusinessException("Maximum OTP verification attempts exceeded. Account has been locked.");
        }

        // Verify OTP
        if (!passwordPolicyService.verifyPassword(request.getOtpCode(), otpRequest.getOtpHash())) {
            otpRequest.incrementAttempt();
            otpRequestRepository.save(otpRequest);
            int otpMaxAttempts = configCacheService.getIntConfig(CacheConstants.OTP_MAX_ATTEMPTS,
                    DEFAULT_OTP_MAX_ATTEMPTS);
            int remainingAttempts = otpMaxAttempts - otpRequest.getAttemptCount();
            throw new BusinessException("Invalid OTP. " + remainingAttempts + " attempts remaining.");
        }

        // OTP verified successfully
        otpRequest.setStatus("VERIFIED");
        otpRequest.setVerifiedAt(LocalDateTime.now());
        otpRequestRepository.save(otpRequest);

        // Generate reset token (short-lived, 10 minutes)
        String resetToken = jwtUtil.generateResetToken(user.getId(), user.getUsername(), request.getRequestId());

        log.info("OTP verified successfully for user: {}", user.getUsername());

        return VerifyOtpResponse.builder()
                .verified(true)
                .message("OTP verified successfully. You can now reset your password.")
                .resetToken(resetToken)
                .build();
    }

    /**
     * Resend OTP
     * FR-AM-1: OTP resend functionality
     */
    @Transactional
    public RequestOtpResponse resendOtp(ResendOtpRequest request) {
        log.info("Resend OTP for request: {}", request.getRequestId());

        // Find previous OTP request
        OtpRequest previousOtpRequest = otpRequestRepository
                .findByRequestId(request.getRequestId())
                .orElseThrow(() -> new BusinessException("Invalid OTP request"));

        // Invalidate previous OTP
        previousOtpRequest.setStatus("EXPIRED");
        otpRequestRepository.save(previousOtpRequest);

        // Generate new OTP using the same process
        RequestOtpRequest newRequest = RequestOtpRequest.builder()
                .username(request.getUsername())
                .purpose(previousOtpRequest.getPurpose())
                .build();

        return requestOtp(newRequest);
    }

    /**
     * Reset Password
     * FR-AM-1: Password reset after OTP verification
     */
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        log.info("Password reset request");

        // Validate reset token
        String requestId;
        Long userId;
        try {
            requestId = jwtUtil.extractRequestId(request.getResetToken());
            userId = jwtUtil.extractUserId(request.getResetToken());
        } catch (Exception e) {
            throw new BusinessException("Invalid or expired reset token");
        }

        // Verify OTP request was verified
        OtpRequest otpRequest = otpRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new BusinessException("Invalid reset token"));

        if (!"VERIFIED".equals(otpRequest.getStatus())) {
            throw new BusinessException("OTP not verified");
        }

        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // Validate password strength
        if (!passwordPolicyService.validatePasswordStrength(request.getNewPassword())) {
            throw new BusinessException(passwordPolicyService.getPasswordValidationMessage());
        }

        // Check password confirmation
        if (!passwordPolicyService.passwordsMatch(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Hash and update password
        String newPasswordHash = passwordPolicyService.hashPassword(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setFirstLogin(false);
        passwordPolicyService.resetFailedLoginAttempts(user);
        authUserRepository.save(user);

        log.info("Password reset successful for user: {}", user.getUsername());

        return ResetPasswordResponse.builder()
                .success(true)
                .message("Password reset successful. You can now login with your new password.")
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            // Validate refresh token
            Long userId = jwtUtil.extractUserId(request.getRefreshToken());

            User user = authUserRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found for provided token"));

            // Get user's roles
            List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
            List<Role> roles = authRoleRepository.findAllById(roleIds);
            List<String> roleCodes = roles.stream().map(Role::getRoleCode).toList();
            List<String> permissionCodes = getUserPermissions(user.getId()).getPermissions().stream()
                    .map(PermissionDTO::getPermissionCode)
                    .collect(Collectors.toList());

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(),
                    roleCodes, permissionCodes);

            // Find and update session with new access token
            UserSession session = sessionManagementService.getSessionByRefreshToken(request.getRefreshToken());

            // Verify session is active
            if (!Boolean.TRUE.equals(session.getIsActive())) {
                throw new UnauthorizedException("Session is not active");
            }

            // Update access token and last activity
            session.setAccessToken(newAccessToken);
            sessionManagementService.updateSessionActivity(session.getSessionId());

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresAt(jwtUtil.getAccessTokenExpirationTime())
                    .refreshExpiresAt(jwtUtil.getRefreshTokenExpirationTime())
                    .build();

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    /**
     * Logout
     */
    @Transactional
    public void logout(String sessionId) {
        sessionManagementService.terminateSession(sessionId, "LOGOUT");
        log.info("User logged out: {}", sessionId);
    }

    /**
     * Get active sessions for user
     */
    public ActiveSessionResponse getActiveSessionsForUser(Long userId) {
        return sessionManagementService.getActiveSessionsForUser(userId);
    }

    /**
     * Terminate session (Admin action)
     */
    @Transactional
    @CacheEvict(value = CacheConstants.ACTIVE_SESSIONS, key = "#userId")
    public void terminateSession(Long userId, String sessionId) {
        sessionManagementService.terminateSession(sessionId, "ADMIN_ACTION");
        log.info("Admin terminated session {} for user ID: {}", sessionId, userId);
    }

    /**
     * Unlock account (Admin action)
     */
    @Transactional
    public String unlockAccount(String username) {
        User user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!passwordPolicyService.isAccountLocked(user)) {
            log.info("Attempted to unlock an already unlocked account for user: {}", username);
            return "Account is not locked";
        }

        passwordPolicyService.unlockAccount(user);
        log.info("Admin unlocked account for user: {}", username);
        return "Account unlocked successfully";
    }

    /**
     * Get account lockout status
     */
    public AccountLockoutStatusResponse getAccountLockoutStatus(String username) {
        User user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        boolean isLocked = passwordPolicyService.isAccountLocked(user);
        String message = isLocked
                ? "Account is locked. Remaining time: " + passwordPolicyService.getRemainingLockoutMinutes(user)
                        + " minutes"
                : "Account is not locked";

        return AccountLockoutStatusResponse.builder().isLocked(isLocked).failedAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getAccountLockedUntil()).message(message).build();
    }

    /**
     * Get user permissions with Redis caching
     * Purpose: Reduce database queries for frequently accessed permissions
     * Old Response Time: 100ms | New Response Time: 10ms (cached)
     *
     * Cache: user_permissions (TTL: 6 hours)
     * Cache Key: user:permissions:{userId}
     * Eviction: On user role/permission changes
     */
    @Cacheable(value = "user_permissions", key = "'user:permissions:' + #userId")
    public UserPermissionsResponse getUserPermissions(Long userId) {
        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Get user's roles
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        List<Role> roles = authRoleRepository.findAllById(roleIds);

        // Get permissions for all roles
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
        List<Permission> permissions = authPermissionRepository.findAllById(permissionIds);

        // Convert to DTOs
        List<RoleDTO> roleDTOs = roles.stream()
                .map(role -> RoleDTO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .roleCode(role.getRoleCode())
                        .build())
                .toList();

        List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(permission -> PermissionDTO.builder()
                        .id(permission.getId())
                        .permissionName(permission.getPermissionName())
                        .permissionCode(permission.getPermissionCode())
                        .resource(permission.getResource())
                        .action(permission.getAction())
                        .description(permission.getDescription())
                        .build())
                .toList();

        return UserPermissionsResponse.builder().userId(userId).username(user.getUsername()).roles(roleDTOs)
                .permissions(permissionDTOs).build();
    }

    /**
     * Validate session
     */
    public boolean validateSession(String sessionId) {
        return sessionManagementService.validateSession(sessionId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generate random OTP
     */
    private String generateOtp() {
        int length = configCacheService.getIntConfig(CacheConstants.OTP_LENGTH, DEFAULT_OTP_LENGTH);
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Send OTP via email using Communication Service (MSG91)
     * Purpose: Non-blocking email sending via centralized Communication Service
     * Old Response Time: 500ms | New Response Time: ~100ms
     *
     * @param email        Recipient email address
     * @param firstName    User's first name
     * @param otp          OTP code to send
     * @param otpRequestId OTP request ID for callback
     */
    private void sendOtpEmailAsync(String email, String firstName, String otp, Long otpRequestId) {
        String templateId = configCacheService.getConfigOrDefault(CacheConstants.MSG91_OTP_TEMPLATE_ID, "global_otp");
        String fromName = configCacheService.getConfigOrDefault(CacheConstants.MSG91_FROM_NAME, "Swastisree Solutions");

        // Build the variables map for the template
        Map<String, String> variables = new HashMap<>();
        variables.put("company_name", fromName);
        variables.put("otp", otp);

        // Build the simplified internal request
        InternalEmailRequest request = new InternalEmailRequest();
        request.setToEmail(email);
        request.setToName(firstName);
        request.setFromName(fromName);
        request.setTemplateId(templateId);
        request.setVariables(variables);

        // Send asynchronously using CompletableFuture
        CompletableFuture.runAsync(() -> {
            try {
                communicationServiceClient.sendMsg91Email(request);
                log.info("OTP email sent successfully via MSG91 to: {}", maskEmail(email));
                updateOtpStatus(otpRequestId, "SENT");
            } catch (Exception e) {
                log.error("Error sending OTP via MSG91 Communication Service: {}", e.getMessage());
                updateOtpStatus(otpRequestId, "FAILED");
            }
        });
    }

    /**
     * Helper method to update OTP request status
     */
    private void updateOtpStatus(Long otpRequestId, String status) {
        otpRequestRepository.findById(otpRequestId).ifPresent(req -> {
            req.setStatus(status);
            if ("SENT".equals(status)) {
                req.setSentAt(LocalDateTime.now());
            }
            otpRequestRepository.save(req);
        });
    }

    /**
     * Mask email for security
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        }
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
    }
}
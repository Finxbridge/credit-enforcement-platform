package com.finx.auth.controller;

import com.finx.common.domain.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import com.finx.auth.service.AuthenticationService;
import com.finx.common.util.EncryptionUtil;
import com.finx.auth.domain.dto.*;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

/**
 * AuthController
 * Purpose: REST API endpoints for authentication and access management
 * Base URL: /api/v1/auth
 * FR: FR-AM-1 through FR-AM-7
 */
@RestController
@RequestMapping("/access/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and access management APIs")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final EncryptionUtil encryptionUtil;

    /**
     * POST /api/v1/auth/login
     * User login with username/password
     * FR-AM-1: Detect first-time login and redirect to password reset
     * FR-AM-2: Normal login for existing users
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Login with username/password. Returns tokens for successful login or password reset requirement for first-time login.")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);

        if (Boolean.TRUE.equals(response.getIsFirstLogin())) {
            return ResponseWrapper.ok("First-time login detected. Password reset required.", response);
        }

        return ResponseWrapper.ok("Login successful", response);
    }

    /**
     * POST /api/v1/auth/request-otp
     * Request OTP for password reset
     * FR-AM-1: Generate and send OTP via email
     */
    @PostMapping("/request-otp")
    @Operation(summary = "Request OTP", description = "Request OTP for password reset. OTP will be sent if email is registered.")
    public ResponseEntity<CommonResponse<RequestOtpResponse>> requestOtp(
            @Valid @RequestBody RequestOtpRequest request) {
        RequestOtpResponse response = authenticationService.requestOtp(request);
        return ResponseWrapper.ok("OTP request processed", response);
    }

    /**
     * POST /api/v1/auth/verify-otp
     * Verify OTP code
     * FR-AM-1: Validate OTP and return reset token
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP code. Returns reset token on successful verification.")
    public ResponseEntity<CommonResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = authenticationService.verifyOtp(request);
        return ResponseWrapper.ok("OTP verified successfully", response);
    }

    /**
     * POST /api/v1/auth/reset-password
     * Reset password with OTP verification
     * FR-AM-1: Complete password reset flow
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token obtained from OTP verification.")
    public ResponseEntity<CommonResponse<ResetPasswordResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordResponse response = authenticationService.resetPassword(request);
        return ResponseWrapper.ok("Password reset successful", response);
    }

    /**
     * POST /api/v1/auth/forget-password
     * Forget password - Initiates password reset by sending OTP to email
     * FR-AM-1: Simplified password reset initiation
     */
    @PostMapping("/forget-password")
    @Operation(summary = "Forget password", description = "Initiate password reset by sending OTP to registered email address.")
    public ResponseEntity<CommonResponse<ForgetPasswordResponse>> forgetPassword(
            @Valid @RequestBody ForgetPasswordRequest request) {
        ForgetPasswordResponse response = authenticationService.forgetPassword(request);
        return ResponseWrapper.ok("Password reset initiated", response);
    }

    /**
     * POST /api/v1/auth/refresh-token
     * Refresh access token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token.")
    public ResponseEntity<CommonResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authenticationService.refreshToken(request);
        return ResponseWrapper.ok("Token refreshed successfully", response);
    }

    /**
     * POST /api/v1/auth/logout
     * User logout - terminates all active sessions for the user by username
     * FR-AM-4: Terminate all sessions for the user
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout user by username and terminate all active sessions across all devices.")
    public ResponseEntity<CommonResponse<Void>> logout(@RequestParam String username) {
        authenticationService.logoutByUsername(username);
        return ResponseWrapper.okMessage("Logout successful. All sessions terminated.");
    }

    /**
     * POST /api/v1/auth/unlock-account/{username}
     * Admin unlock after failed attempts
     * FR-AM-6: Administrative account unlock
     */
    @PostMapping("/unlock-account/{username}")
    @Operation(summary = "Unlock account", description = "Unlock user account after lockout (Admin only).")
    public ResponseEntity<CommonResponse<String>> unlockAccount(@PathVariable String username) {
        String message = authenticationService.unlockAccount(username);
        return ResponseWrapper.ok(message, message);
    }

    /**
     * GET /api/v1/auth/lockout-status/{username}
     * Check account lockout status
     * FR-AM-6: Check if account is locked
     */
    @GetMapping("/lockout-status/{username}")
    @Operation(summary = "Get lockout status", description = "Check if account is locked and get lockout details.")
    public ResponseEntity<CommonResponse<AccountLockoutStatusResponse>> getLockoutStatus(
            @PathVariable String username) {
        AccountLockoutStatusResponse response = authenticationService.getAccountLockoutStatus(username);
        return ResponseWrapper.ok("Lockout status retrieved", response);
    }

    /**
     * POST /api/v1/auth/encrypt
     * Encrypts a given string value.
     */
    @PostMapping("/encrypt")
    @Operation(summary = "Encrypt value", description = "Encrypts a given string value using the configured encryption utility.")
    public ResponseEntity<CommonResponse<EncryptionResponse>> encrypt(@Valid @RequestBody EncryptionRequest request) {
        String encryptedValue = encryptionUtil.encrypt(request.getValue());
        return ResponseWrapper.ok("Value encrypted successfully",
                EncryptionResponse.builder().encryptedValue(encryptedValue).build());
    }

    /**
     * POST /api/v1/auth/decrypt
     * Decrypts a given encrypted string value.
     */
    @PostMapping("/decrypt")
    @Operation(summary = "Decrypt value", description = "Decrypts a given encrypted string value using the configured encryption utility.")
    public ResponseEntity<CommonResponse<EncryptionResponse>> decrypt(@Valid @RequestBody EncryptionRequest request) {
        String decryptedValue = encryptionUtil.decrypt(request.getValue());
        return ResponseWrapper.ok("Value decrypted successfully",
                EncryptionResponse.builder().decryptedValue(decryptedValue).build());
    }
}
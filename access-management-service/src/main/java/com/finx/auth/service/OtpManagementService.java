package com.finx.auth.service;

import com.finx.auth.domain.entity.User;
import com.finx.auth.repository.AuthUserRepository;
import com.finx.auth.repository.OtpRequestRepository;
import com.finx.common.model.OtpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * OtpManagementService
 * Purpose: Manages OTP-related operations that require separate transaction
 * handling
 *
 * This service is separate from AuthenticationService to ensure proper
 * transaction
 * propagation for operations that need REQUIRES_NEW propagation (e.g.,
 * incrementing
 * OTP attempts should persist even if the parent transaction rolls back).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpManagementService {

    private final OtpRequestRepository otpRequestRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordPolicyService passwordPolicyService;

    /**
     * Increment OTP attempt count and save in a new transaction.
     * This ensures the attempt count is persisted even if the calling transaction
     * rolls back.
     *
     * @param otpRequest The OTP request to increment
     * @return The updated OTP request with incremented attempt count
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OtpRequest incrementOtpAttemptAndSave(OtpRequest otpRequest) {
        otpRequest.incrementAttempt();
        OtpRequest saved = otpRequestRepository.save(otpRequest);
        log.debug("OTP attempt incremented for request: {}. Current attempts: {}/{}",
                otpRequest.getRequestId(), saved.getAttemptCount(), saved.getMaxAttempts());
        return saved;
    }

    /**
     * Lock user account after max OTP verification failures in a new transaction.
     * This ensures the account lock is persisted even if the calling transaction
     * rolls back.
     *
     * @param user The user to lock
     */
    @SuppressWarnings("null")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockAccountAfterOtpFailure(User user) {
        passwordPolicyService.lockAccount(user);
        authUserRepository.save(user);
        log.warn("Account locked due to max OTP verification failures for user: {}", user.getUsername());
    }
}

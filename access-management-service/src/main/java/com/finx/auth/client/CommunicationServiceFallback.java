package com.finx.auth.client;

import com.finx.common.domain.dto.InternalEmailRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for Communication Service Client
 * Provides graceful degradation when Communication Service is unavailable
 */
@Component
@Slf4j
public class CommunicationServiceFallback implements CommunicationServiceClient {

    @Override
    public EmailResponse sendEmail(EmailSendRequest request) {
        log.error("Communication service is unavailable. Email not sent to: {}", request.to());
        // Return failed response instead of throwing exception
        // This allows OTP flow to continue - user can request resend
        return new EmailResponse(
                "FAILED",
                "Communication service is temporarily unavailable. Please try again.");
    }

    @Override
    public void sendMsg91Email(InternalEmailRequest request) {
        log.error("Communication service is unavailable. Msg91 Email not sent.");
        // No return value for void method
    }
}

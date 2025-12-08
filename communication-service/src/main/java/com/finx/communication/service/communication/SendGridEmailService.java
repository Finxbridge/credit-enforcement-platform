package com.finx.communication.service.communication;

import com.finx.communication.service.IntegrationCacheService;
import com.finx.communication.domain.dto.email.*;
import com.finx.communication.domain.entity.EmailMessage;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.EmailMessageRepository;
import com.finx.communication.util.TemplateVariableReplacer;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * SendGrid Email Service
 * Handles email sending via SendGrid Email API
 *
 * Integration: SENDGRID_EMAIL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendGridEmailService {

    private final IntegrationCacheService integrationCacheService;
    private final EmailMessageRepository emailMessageRepository;

    private static final String INTEGRATION_NAME = "SENDGRID_EMAIL";
    private static final String DEFAULT_FROM_EMAIL = "noreply@finxbridge.com";
    private static final String DEFAULT_FROM_NAME = "FinX Bridge";

    public EmailResponse sendEmail(EmailSendRequest request) {
        // 1. Early validation - fail fast
        if (request.getTo() == null || request.getTo().trim().isEmpty()) {
            log.error("No recipient email provided in SendGrid email request");
            return EmailResponse.builder()
                    .messageId(UUID.randomUUID().toString())
                    .status("FAILED")
                    .message("Recipient email is required")
                    .providerResponse("Validation Error: No recipient email")
                    .build();
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            log.error("No subject provided in SendGrid email request");
            return EmailResponse.builder()
                    .messageId(UUID.randomUUID().toString())
                    .status("FAILED")
                    .message("Email subject is required")
                    .providerResponse("Validation Error: No subject")
                    .build();
        }

        log.info("Sending email to: {}", request.getTo());

        // 2. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 3. Replace variables in body if present (optimized - skip if no variables)
        String finalBody = request.getBodyText();
        if (request.getVariables() != null && !request.getVariables().isEmpty() && finalBody != null) {
            finalBody = TemplateVariableReplacer.replace(finalBody, request.getVariables());
        }

        // 4. Send email using SendGrid SDK
        Response response = sendEmailViaSendGrid(request, finalBody, config);

        // 5. Generate message ID
        String messageId = UUID.randomUUID().toString();

        // 6. Check if email was sent successfully (2xx status codes)
        int statusCode = response.getStatusCode();
        String status;
        String message;

        if (statusCode >= 200 && statusCode < 300) {
            status = "SENT";
            message = "Email sent successfully";
            // 7. Save to database asynchronously (only on success)
            saveEmailMessageAsync(request, response, messageId);
        } else {
            status = "FAILED";
            message = "Email sending failed";
            log.error("SendGrid returned error status code: {}, Body: {}", statusCode, response.getBody());
        }

        return EmailResponse.builder()
                .messageId(messageId)
                .status(status)
                .message(message)
                .providerResponse(String.format("StatusCode: %d, Body: %s",
                        response.getStatusCode(), response.getBody()))
                .build();
    }

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    private Response sendEmailViaSendGrid(EmailSendRequest request, String finalBody,
            ThirdPartyIntegrationMaster config) {
        try {
            // Get API key from cache config (not encrypted for now)
            String apiKey = config.getApiKeyEncrypted();

            // Build email components - all dynamic from request
            Email from = new Email(
                    request.getFromEmail() != null ? request.getFromEmail() : DEFAULT_FROM_EMAIL,
                    request.getFromName() != null ? request.getFromName() : DEFAULT_FROM_NAME);

            String subject = request.getSubject();
            Email to = new Email(request.getTo());

            // Use text/plain content with dynamic body
            Content content = new Content("text/plain", finalBody != null ? finalBody : "");

            // Build mail object
            Mail mail = new Mail(from, subject, to, content);

            // Add HTML content if available (optimized - skip variable replacement if
            // empty)
            if (request.getBodyHtml() != null) {
                String htmlBody = request.getBodyHtml();
                if (request.getVariables() != null && !request.getVariables().isEmpty()) {
                    htmlBody = TemplateVariableReplacer.replace(htmlBody, request.getVariables());
                }
                mail.addContent(new Content("text/html", htmlBody));
            }

            // Initialize SendGrid with API key from config
            SendGrid sg = new SendGrid(apiKey);

            // Set data residency if configured (for EU regions)
            String region = config.getConfigValueAsString("region");
            if (region != null && region.equalsIgnoreCase("EU")) {
                sg.setDataResidency("eu");
            }

            // Build and send request
            Request sgRequest = new Request();
            sgRequest.setMethod(Method.POST);
            sgRequest.setEndpoint("mail/send");
            sgRequest.setBody(mail.build());

            Response response = sg.api(sgRequest);

            log.info("SendGrid API Response - StatusCode: {}, Body: {}",
                    response.getStatusCode(), response.getBody());

            return response;

        } catch (IOException ex) {
            log.error("SendGrid API call failed", ex);
            throw new ApiCallException("Failed to send email via SendGrid", ex);
        } catch (Exception e) {
            log.error("Error sending email", e);
            throw new ApiCallException("Failed to send email", e);
        }
    }

    /**
     * Save email message to database (Async)
     * Uses @Async to not block the API response
     * Executes in background thread pool configured in AsyncConfig
     */
    @SuppressWarnings("null")
    @Async("taskExecutor")
    private void saveEmailMessageAsync(EmailSendRequest request, Response response, String messageId) {
        try {
            log.debug("Async saving email message with ID: {}", messageId);

            // Use dynamic from email and name from request, fallback to defaults
            String fromEmail = request.getFromEmail() != null ? request.getFromEmail() : DEFAULT_FROM_EMAIL;
            String fromName = request.getFromName() != null ? request.getFromName() : DEFAULT_FROM_NAME;

            String providerResponse = String.format("StatusCode: %d, Body: %s, Headers: %s",
                    response.getStatusCode(), response.getBody(), response.getHeaders());

            EmailMessage emailMessage = EmailMessage.builder()
                    .messageId(messageId)
                    .toEmail(request.getTo())
                    .emailCc(request.getCc())
                    .emailBcc(request.getBcc())
                    .fromEmail(fromEmail)
                    .fromName(fromName)
                    .subject(request.getSubject())
                    .bodyHtml(request.getBodyHtml())
                    .bodyText(request.getBodyText())
                    .templateCode(request.getTemplateId())
                    .provider("SENDGRID")
                    .status("SENT")
                    .campaignId(request.getCampaignId())
                    .caseId(request.getCaseId())
                    .userId(request.getUserId())
                    .providerResponse(providerResponse)
                    .sentAt(LocalDateTime.now())
                    .build();

            emailMessageRepository.save(emailMessage);
            log.info("Successfully saved email message with ID: {} (async)", messageId);
        } catch (Exception e) {
            log.error("Failed to save email message to database (async): {}", messageId, e);
            // Don't throw - async method, already logged
        }
    }

    /**
     * Send email asynchronously
     * Purpose: Non-blocking email sending for OTP and notifications
     * Response Time: Returns immediately (~5ms), email sent in background
     *
     * @param request Email send request
     * @return CompletableFuture with email response
     */
    @Async("taskExecutor")
    public CompletableFuture<EmailResponse> sendEmailAsync(EmailSendRequest request) {
        log.info("Sending email asynchronously to: {}", request.getTo());

        try {
            // Call synchronous email send method
            EmailResponse response = sendEmail(request);

            log.info("Async email sent successfully to: {}", request.getTo());
            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Async email send failed to: {}, Error: {}", request.getTo(), e.getMessage());

            // Return failed response instead of throwing exception
            EmailResponse errorResponse = EmailResponse.builder()
                    .messageId(UUID.randomUUID().toString())
                    .status("FAILED")
                    .message("Failed to send email: " + e.getMessage())
                    .providerResponse("Error: " + e.getMessage())
                    .build();

            return CompletableFuture.completedFuture(errorResponse);
        }
    }
}

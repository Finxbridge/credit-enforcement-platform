package com.finx.communication.service.communication;

import com.finx.communication.constants.CacheConstants;
import com.finx.communication.domain.dto.InternalEmailRequest;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.service.ConfigCacheService;
import com.finx.communication.service.IntegrationCacheService;
import com.finx.communication.domain.dto.email.Msg91Attachment;
import com.finx.communication.domain.dto.email.Msg91From;
import com.finx.communication.domain.dto.email.Msg91EmailResponse;
import com.finx.communication.domain.dto.email.Msg91EmailSendRequest;
import com.finx.communication.domain.dto.email.Msg91EmailTo;
import com.finx.communication.domain.dto.email.Msg91Recipient;
import com.finx.communication.domain.dto.email.Msg91ReplyTo;
import com.finx.communication.repository.EmailMessageRepository;
import com.finx.communication.domain.entity.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * MSG91 Email Service
 * Handles email sending via MSG91 Email API v5
 *
 * Integration: MSG91_EMAIL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Msg91EmailService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final ConfigCacheService configCacheService;
    private final EmailMessageRepository emailMessageRepository;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * Transforms the internal email request and sends it via MSG91.
     */
    @SuppressWarnings("null")
    public com.finx.communication.domain.dto.email.Msg91EmailResponse sendEmail(InternalEmailRequest internalRequest) {
        try {
            // 1. Get integration config from cache (URL + API Key)
            ThirdPartyIntegrationMaster config = getIntegrationConfig();
            String fromEmail = configCacheService.getConfigOrDefault(CacheConstants.EMAIL_FROM_ADDRESS,
                    "info@mail.swastisree.com");
            String domain = fromEmail.substring(fromEmail.indexOf("@") + 1);

            // 2. Transform InternalEmailRequest to Msg91EmailSendRequest
            Msg91EmailSendRequest msg91Request = transformRequest(internalRequest, fromEmail, domain);

            log.info("Sending transformed email request via MSG91 to: {}", internalRequest.getToEmail());

            // 3. Use MSG91 API URL directly from database (full endpoint)
            String url = config.getApiEndpoint();

            // 4. Get WebClient timeout from cached config
            int timeoutSeconds = configCacheService.getIntConfig(CacheConstants.WEBCLIENT_RESPONSE_TIMEOUT,
                    DEFAULT_TIMEOUT_SECONDS);
            Duration timeout = Duration.ofSeconds(timeoutSeconds);

            // 5. Call MSG91 API (use API key directly - no encryption)
            log.debug("MSG91 Request Payload: {}", msg91Request);
            String rawResponse = webClient.post()
                    .uri(url)
                    .header("authkey", config.getApiKeyEncrypted())
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(msg91Request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .doOnError(error -> log.error("MSG91 email send failed: {}", error.getMessage()))
                    .block();

            log.info("MSG91 send email response: {}", rawResponse);

            // 6. Parse response
            Msg91EmailResponse response = objectMapper.readValue(rawResponse, Msg91EmailResponse.class);

            // 7. Save to database (only if successful)
            if ("success".equalsIgnoreCase(response.getStatus()) && !response.isHasError()) {
                saveEmailMessage(msg91Request, response, rawResponse);
            }

            // 8. Return raw MSG91 response
            return response;

        } catch (Exception e) {
            log.error("Error sending email via MSG91: {}", e.getMessage(), e);
            // This will be caught by the GlobalExceptionHandler
            throw new ApiCallException("Failed to send email via MSG91: " + e.getMessage(), e);
        }
    }

    /**
     * Transforms and sends email asynchronously.
     */
    @Async("taskExecutor")
    public CompletableFuture<com.finx.communication.domain.dto.email.Msg91EmailResponse> sendEmailAsync(
            InternalEmailRequest internalRequest) {
        log.info("Sending email asynchronously via MSG91 to: {}", internalRequest.getToEmail());
        return CompletableFuture.completedFuture(sendEmail(internalRequest));
    }

    // ==================== Helper Methods ====================

    private Msg91EmailSendRequest transformRequest(InternalEmailRequest internalRequest, String fromEmail,
            String domain) {
        // Declare fromName at the beginning
        String fromName = configCacheService.getConfigOrDefault(CacheConstants.EMAIL_FROM_NAME, "Support Team");

        // Create the complex recipient structure
        String recipientName = internalRequest.getToName() != null && !internalRequest.getToName().isBlank()
                ? internalRequest.getToName()
                : internalRequest.getToEmail().split("@")[0];
        Msg91EmailTo to = new Msg91EmailTo(recipientName, internalRequest.getToEmail());
        Msg91Recipient recipient = new Msg91Recipient();
        recipient.setTo(List.of(to));

        Map<String, String> msg91Variables = internalRequest.getVariables();
        recipient.setVariables(msg91Variables);

        // Create the From object
        Msg91From from = new Msg91From(fromName, fromEmail); // Use the already declared fromName

        // Build the final request
        Msg91EmailSendRequest msg91Request = new Msg91EmailSendRequest();
        msg91Request.setRecipients(List.of(recipient));
        msg91Request.setFrom(from);
        msg91Request.setDomain(domain);
        msg91Request.setTemplateId(internalRequest.getTemplateId());

        // Handle optional reply_to
        if (internalRequest.getReplyTo() != null && !internalRequest.getReplyTo().isEmpty()) { // Changed to isEmpty()
            List<Msg91ReplyTo> msg91ReplyToList = internalRequest.getReplyTo().stream()
                    .map(Msg91ReplyTo::new) // Assuming Msg91ReplyTo has a constructor that takes a String email
                    .collect(Collectors.toList());
            msg91Request.setReplyTo(msg91ReplyToList);
        }

        // Handle attachments
        if (internalRequest.getAttachments() != null && !internalRequest.getAttachments().isEmpty()) {
            // Assuming InternalEmailRequest.Attachment can be mapped to Msg91Attachment
            // You might need a more complex mapping here depending on Msg91Attachment
            // structure
            List<Msg91Attachment> msg91Attachments = internalRequest.getAttachments().stream()
                    .map(att -> new Msg91Attachment(att.getFilename(), att.getContent(), att.getContentType()))
                    .collect(Collectors.toList());
            msg91Request.setAttachments(msg91Attachments);
        }
        // If internalRequest.getAttachments() is null or empty,
        // msg91Request.attachments will remain null,
        // which will omit the field from the JSON if ObjectMapper is configured with
        // JsonInclude.Include.NON_NULL.

        return msg91Request;
    }

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        Object cachedObject = integrationCacheService.getIntegrationConfig(CacheConstants.MSG91_EMAIL).orElse(null);
        if (cachedObject != null) {
            if (cachedObject instanceof ThirdPartyIntegrationMaster) {
                return (ThirdPartyIntegrationMaster) cachedObject;
            } else {
                return objectMapper.convertValue(cachedObject, ThirdPartyIntegrationMaster.class);
            }
        }
        throw new ConfigurationNotFoundException("MSG91 Email integration config not found");
    }

    /**
     * Create Email Template in MSG91
     * POST /api/v5/email/templates
     */
    @SuppressWarnings("null")
    public Map<String, Object> createTemplate(com.finx.communication.domain.dto.email.EmailTemplateCreateRequest request) {
        log.info("Creating email template: {}", request.getName());

        try {
            // 1. Get integration config
            ThirdPartyIntegrationMaster config = getIntegrationConfig();

            // 2. Get URL from config_json
            String url = config.getConfigValueAsString("create_template_url");

            // 3. Build request body matching MSG91 API exactly
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("name", request.getName());
            if (request.getSlug() != null && !request.getSlug().isEmpty()) {
                requestBody.put("slug", request.getSlug());
            }
            requestBody.put("subject", request.getSubject());
            requestBody.put("body", request.getBody());

            // 4. Call MSG91 API (use API key directly - no encryption)
            String response = webClient.post()
                    .uri(url)
                    .header("authkey", config.getApiKeyEncrypted())
                    .header("Accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Email template creation failed: {}", error.getMessage()))
                    .block();

            log.info("Email template creation response: {}", response);

            // 5. Parse and return response
            return objectMapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            log.error("Error creating email template: {}", e.getMessage(), e);
            throw new ApiCallException("Failed to create email template: " + e.getMessage(), e);
        }
    }

    private void saveEmailMessage(Msg91EmailSendRequest msg91Request, Msg91EmailResponse msg91Response,
            String rawResponse) {
        EmailMessage emailMessage = new EmailMessage();
        // Assuming single recipient for simplicity, adjust if multiple recipients are
        // supported
        if (msg91Request.getRecipients() != null && !msg91Request.getRecipients().isEmpty()) {
            Msg91Recipient recipient = msg91Request.getRecipients().get(0);
            if (recipient.getTo() != null && !recipient.getTo().isEmpty()) {
                emailMessage.setToEmail(recipient.getTo().get(0).getEmail());
            }
        }
        emailMessage.setFromEmail(msg91Request.getFrom().getEmail());
        emailMessage.setSubject("Email from template: " + msg91Request.getTemplateId()); // Placeholder subject
        emailMessage.setBodyText("Email sent via MSG91"); // Placeholder body
        emailMessage.setStatus("SENT");
        emailMessage.setMessageId(msg91Response.getData().getUniqueId());
        emailMessage.setProviderMessageId(msg91Response.getData().getUniqueId());
        emailMessage.setProviderResponse(rawResponse);
        emailMessage.setSentAt(LocalDateTime.now());
        emailMessageRepository.save(emailMessage);
    }
}
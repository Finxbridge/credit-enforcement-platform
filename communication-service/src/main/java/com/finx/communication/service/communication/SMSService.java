package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.IntegrationCacheService;
import com.finx.communication.domain.dto.sms.*;
import com.finx.communication.domain.entity.SmsMessage;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.SmsMessageRepository;
import com.finx.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SMS Service - Msg91 Integration
 * Handles SMS sending with dynamic variables support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SMSService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final SmsMessageRepository smsMessageRepository;
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;

    private static final String INTEGRATION_NAME = "MSG91_SMS";
    private static final String AUTH_KEY = "authkey";
    private static final String ACCEPT_TYPE = "application/json";
    private static final String ACCEPT = "accept";
    private static final String RAW_RESPONSE = "raw_response";

    /**
     * Send SMS with dynamic variables
     * Supports multiple recipients with different variable values
     */
    public SmsResponse sendSms(SmsSendRequest request) {
        log.info("Sending SMS to {} recipients", request.getRecipients().size());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build request body with dynamic variables
        Map<String, Object> requestBody = buildSmsRequestBody(request);

        // 3. Build URL
        String url = config.getApiEndpoint() + "/api/v5/flow";

        // 4. Call Msg91 API
        String response = callMsg91Api(url, requestBody, config);

        // 5. Save to database
        List<String> messageIds = saveSmsMessages(request, response);

        // 6. Return response
        return SmsResponse.builder()
                .status("SENT")
                .message("SMS sent successfully to " + request.getRecipients().size() + " recipients")
                .messageIds(messageIds)
                .providerResponse(response)
                .build();
    }

    /**
     * Create SMS template
     */
    public Map<String, Object> createTemplate(SmsCreateTemplateRequest request) {
        log.info("Creating SMS template: {}", request.getTemplateName());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build URL
        String url = config.getApiEndpoint() + "/api/v5/sms/addTemplate";

        // 3. Build multipart form data
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("template", request.getTemplate());
        builder.part("sender_id", request.getSenderId());
        builder.part("template_name", request.getTemplateName());
        builder.part("dlt_template_id", request.getDltTemplateId());
        builder.part("smsType", request.getSmsType() != null ? request.getSmsType() : "NORMAL");

        // 4. Call Msg91 API
        String response = webClient.post()
                .uri(url)
                .header(AUTH_KEY, encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                .header(ACCEPT, ACCEPT_TYPE)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Template creation failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to create template", error)))
                .block();

        log.info("Template creation response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of(RAW_RESPONSE, response);
        }
    }

    /**
     * Get template details
     */
    public Map<String, Object> getTemplateDetails(String templateId) {
        log.info("Fetching template details for: {}", templateId);

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build URL
        String url = config.getApiEndpoint() + "/api/v5/sms/getTemplateVersions";

        // 3. Build request body
        Map<String, Object> requestBody = Map.of("template_id", templateId);

        // 4. Call Msg91 API
        String response = webClient.post()
                .uri(url)
                .header(AUTH_KEY, encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                .header(ACCEPT, ACCEPT_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Template details response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of(RAW_RESPONSE, response);
        }
    }

    /**
     * Get SMS logs
     */
    public Map<String, Object> getLogs(String startDate, String endDate) {
        log.info("Fetching SMS logs from {} to {}", startDate, endDate);

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build URL with query parameters
        String url = config.getApiEndpoint() + "/api/v5/report/logs/p/sms" +
                "?startDate=" + startDate +
                "&endDate=" + endDate;

        // 3. Call Msg91 API
        String response = webClient.post()
                .uri(url)
                .header(AUTH_KEY, encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                .header(ACCEPT, ACCEPT_TYPE)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("SMS logs response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of(RAW_RESPONSE, response);
        }
    }

    // ==================== Helper Methods ====================

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    /**
     * Build SMS request body with dynamic variables for each recipient
     */
    private Map<String, Object> buildSmsRequestBody(SmsSendRequest request) {
        Map<String, Object> body = new HashMap<>();

        body.put("template_id", request.getTemplateId());

        if (request.getShortUrl() != null) {
            body.put("short_url", request.getShortUrl());
        }

        if (request.getShortUrlExpiry() != null) {
            body.put("short_url_expiry", request.getShortUrlExpiry());
        }

        if (request.getRealTimeResponse() != null) {
            body.put("realTimeResponse", request.getRealTimeResponse());
        }

        // Build recipients array with dynamic variables
        List<Map<String, Object>> recipientsList = new ArrayList<>();
        for (SmsRecipient recipient : request.getRecipients()) {
            Map<String, Object> recipientData = new HashMap<>();
            recipientData.put("mobiles", recipient.getMobile());

            // Add dynamic variables (VAR1, VAR2, VAR3, etc.)
            if (recipient.getVariables() != null) {
                recipientData.putAll(recipient.getVariables());
            }

            recipientsList.add(recipientData);
        }

        body.put("recipients", recipientsList);

        return body;
    }

    private String callMsg91Api(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            return webClient.post()
                    .uri(url)
                    .header(AUTH_KEY, encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                    .header(ACCEPT, ACCEPT_TYPE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Msg91 API call failed", error))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to send SMS", error)))
                    .block();
        } catch (Exception e) {
            throw new ApiCallException("Failed to call Msg91 API", e);
        }
    }

    private List<String> saveSmsMessages(SmsSendRequest request, String response) {
        List<String> messageIds = new ArrayList<>();

        for (SmsRecipient recipient : request.getRecipients()) {
            String messageId = UUID.randomUUID().toString();
            messageIds.add(messageId);

            SmsMessage smsMessage = SmsMessage.builder()
                    .messageId(messageId)
                    .mobile(recipient.getMobile())
                    .templateCode(request.getTemplateId())
                    .messageContent("SMS with template: " + request.getTemplateId())
                    .provider("MSG91")
                    .status("SENT")
                    .campaignId(request.getCampaignId())
                    .caseId(request.getCaseId())
                    .userId(request.getUserId())
                    .providerResponse(response)
                    .sentAt(LocalDateTime.now())
                    .build();

            smsMessageRepository.save(smsMessage);
        }

        return messageIds;
    }
}

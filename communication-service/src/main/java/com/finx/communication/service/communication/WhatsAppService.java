package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.IntegrationCacheService;
import com.finx.communication.domain.dto.whatsapp.*;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.communication.domain.entity.WhatsAppMessage;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.WhatsAppMessageRepository;
import com.finx.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

/**
 * WhatsApp Service - Msg91 Integration
 * Handles WhatsApp messages with dynamic components
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;

    private static final String INTEGRATION_NAME = "MSG91_WHATSAPP";

    /**
     * Send WhatsApp message with dynamic components
     */
    public WhatsAppResponse sendWhatsApp(WhatsAppSendRequest request) {
        log.info("Sending WhatsApp to {} recipient groups", request.getRecipients().size());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build request body with dynamic components
        Map<String, Object> requestBody = buildWhatsAppRequestBody(request);

        // 3. Build URL
        String url = config.getApiEndpoint() + "/api/v5/whatsapp/whatsapp-outbound-message/bulk/";

        // 4. Call Msg91 API
        String response = callMsg91Api(url, requestBody, config);

        // 5. Save to database
        List<String> messageIds = saveWhatsAppMessages(request, response);

        // 6. Return response
        return WhatsAppResponse.builder()
                .status("SENT")
                .message("WhatsApp sent successfully")
                .messageIds(messageIds)
                .providerResponse(response)
                .build();
    }

    /**
     * Create WhatsApp template
     */
    @SuppressWarnings("null")
    public Map<String, Object> createTemplate(Map<String, Object> templateRequest) {
        log.info("Creating WhatsApp template");

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build URL
        String url = config.getApiEndpoint() + "/api/v5/whatsapp/client-panel-template/";

        // 3. Call Msg91 API
        String response = webClient.post()
                .uri(url)
                .header("authkey", encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(templateRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Template creation response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("raw_response", response);
        }
    }

    // ==================== Helper Methods ====================

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    /**
     * Build WhatsApp request body with dynamic components for each recipient
     */
    private Map<String, Object> buildWhatsAppRequestBody(WhatsAppSendRequest request) {
        Map<String, Object> body = new HashMap<>();

        body.put("integrated_number", request.getIntegratedNumber());
        body.put("content_type", "template");

        // Build payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("type", "template");

        // Build template
        Map<String, Object> template = new HashMap<>();
        template.put("name", request.getTemplateName());

        // Language
        Map<String, String> language = new HashMap<>();
        language.put("code", request.getLanguageCode() != null ? request.getLanguageCode() : "en");
        language.put("policy", "deterministic");
        template.put("language", language);

        // Build to_and_components array
        List<Map<String, Object>> toAndComponents = new ArrayList<>();
        for (WhatsAppRecipient recipient : request.getRecipients()) {
            Map<String, Object> recipientData = new HashMap<>();
            recipientData.put("to", recipient.getTo());

            // Add dynamic components
            if (recipient.getComponents() != null) {
                recipientData.put("components", recipient.getComponents());
            }

            toAndComponents.add(recipientData);
        }

        template.put("to_and_components", toAndComponents);
        payload.put("template", template);

        body.put("payload", payload);

        return body;
    }

    @SuppressWarnings("null")
    private String callMsg91Api(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            return webClient.post()
                    .uri(url)
                    .header("authkey", encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Msg91 API call failed", error))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to send WhatsApp", error)))
                    .block();
        } catch (Exception e) {
            throw new ApiCallException("Failed to call Msg91 API", e);
        }
    }

    @SuppressWarnings("null")
    private List<String> saveWhatsAppMessages(WhatsAppSendRequest request, String response) {
        List<String> messageIds = new ArrayList<>();

        for (WhatsAppRecipient recipient : request.getRecipients()) {
            for (String mobile : recipient.getTo()) {
                String messageId = UUID.randomUUID().toString();
                messageIds.add(messageId);

                WhatsAppMessage whatsAppMessage = WhatsAppMessage.builder()
                        .messageId(messageId)
                        .mobile(mobile)
                        .templateName(request.getTemplateName())
                        .language(request.getLanguageCode() != null ? request.getLanguageCode() : "en")
                        .provider("MSG91")
                        .status("SENT")
                        .campaignId(request.getCampaignId())
                        .caseId(request.getCaseId())
                        .userId(request.getUserId())
                        .providerResponse(response)
                        .sentAt(LocalDateTime.now())
                        .build();

                whatsAppMessageRepository.save(whatsAppMessage);
            }
        }

        return messageIds;
    }
}

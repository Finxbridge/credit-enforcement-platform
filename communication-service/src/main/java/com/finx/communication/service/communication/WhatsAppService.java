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
        log.info("Sending WhatsApp to {} recipients", request.getTo().size());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build request body with dynamic components (transforms to Msg91 format)
        Map<String, Object> requestBody = buildWhatsAppRequestBody(request, config);

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
     * Build WhatsApp request body - transforms user input to Msg91 API format
     * Loads namespace and integrated_number from database config
     */
    private Map<String, Object> buildWhatsAppRequestBody(WhatsAppSendRequest request, ThirdPartyIntegrationMaster config) {
        // Extract configuration from config_json
        String namespace = config.getConfigValueAsString("namespace");
        String integratedNumber = config.getConfigValueAsString("integrated_number");

        if (namespace == null || integratedNumber == null) {
            throw new ConfigurationNotFoundException(
                    "WhatsApp namespace and integrated_number must be configured in database config_json");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("integrated_number", integratedNumber);
        body.put("content_type", "template");

        // Build payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("type", "template");

        // Build template (order matches Msg91 API curl exactly)
        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", request.getTemplateId());

        // Language
        Map<String, String> language = new LinkedHashMap<>();
        if (request.getLanguage() != null) {
            language.put("code", request.getLanguage().getCode());
            language.put("policy", request.getLanguage().getPolicy());
        } else {
            // Default language
            language.put("code", "en");
            language.put("policy", "deterministic");
        }
        template.put("language", language);

        // Namespace (after language to match curl order)
        template.put("namespace", namespace);

        // Build to_and_components array (single element from simplified input)
        List<Map<String, Object>> toAndComponents = new ArrayList<>();
        Map<String, Object> recipientData = new HashMap<>();
        recipientData.put("to", request.getTo());

        // Add dynamic components if present
        if (request.getComponents() != null && !request.getComponents().isEmpty()) {
            recipientData.put("components", request.getComponents());
        }

        toAndComponents.add(recipientData);

        template.put("to_and_components", toAndComponents);
        payload.put("template", template);
        body.put("payload", payload);

        log.debug("Built Msg91 request body: {}", body);
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

        // Save a message record for each recipient
        for (String mobile : request.getTo()) {
            String messageId = UUID.randomUUID().toString();
            messageIds.add(messageId);

            String languageCode = request.getLanguage() != null
                    ? request.getLanguage().getCode()
                    : "en";

            WhatsAppMessage whatsAppMessage = WhatsAppMessage.builder()
                    .messageId(messageId)
                    .mobile(mobile)
                    .templateName(request.getTemplateId())
                    .language(languageCode)
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

        return messageIds;
    }
}

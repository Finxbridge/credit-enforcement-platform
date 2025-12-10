package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.communication.service.IntegrationCacheService;
import com.finx.communication.domain.dto.whatsapp.*;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.domain.entity.WhatsAppMessage;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.WhatsAppMessageRepository;
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

    private static final String INTEGRATION_NAME = "MSG91_WHATSAPP";

    /**
     * Send WhatsApp message with dynamic components
     */
    public WhatsAppResponse sendWhatsApp(WhatsAppSendRequest request) {
        log.info("========== WHATSAPP SEND REQUEST START ==========");
        log.info("Sending WhatsApp to {} recipients", request.getTo().size());
        log.info("Incoming Request - TemplateId: {}, To: {}", request.getTemplateId(), request.getTo());
        log.info("Language: {}", request.getLanguage());
        log.info("Components: {}", request.getComponents());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();
        log.info("Config - Endpoint: {}", config.getApiEndpoint());
        log.info("Config - Namespace: {}", config.getConfigValueAsString("namespace"));
        log.info("Config - IntegratedNumber: {}", config.getConfigValueAsString("integrated_number"));

        // 2. Build request body with dynamic components (transforms to Msg91 format)
        Map<String, Object> requestBody = buildWhatsAppRequestBody(request, config);

        // Log the exact JSON being sent to MSG91
        try {
            String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
            log.info("========== MSG91 WHATSAPP REQUEST PAYLOAD ==========\n{}", jsonPayload);
        } catch (Exception e) {
            log.warn("Could not serialize request body for logging", e);
        }

        // 3. Use URL directly from database (full endpoint)
        String url = config.getApiEndpoint();

        // 4. Call Msg91 API
        String response = callMsg91Api(url, requestBody, config);
        log.info("========== MSG91 WHATSAPP RESPONSE ==========\n{}", response);

        // 5. Save to database
        List<String> messageIds = saveWhatsAppMessages(request, response);

        log.info("========== WHATSAPP SEND REQUEST END ==========");

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
     * integrated_number is read from database config_json
     */
    @SuppressWarnings("null")
    public Map<String, Object> createTemplate(WhatsAppCreateTemplateRequest request) {
        log.info("Creating WhatsApp template: {}", request.getTemplateName());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL and integrated_number from config_json
        String url = config.getConfigValueAsString("create_template_url");
        String integratedNumber = config.getConfigValueAsString("integrated_number");

        log.info("Using integrated_number: {} (from config_json)", integratedNumber);

        // 3. Build request body with integrated_number from config
        Map<String, Object> requestBody = buildCreateTemplateRequestBody(request, integratedNumber);

        // Log the exact JSON being sent to MSG91
        try {
            String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
            log.info("========== MSG91 CREATE TEMPLATE REQUEST PAYLOAD ==========\n{}", jsonPayload);
        } catch (Exception e) {
            log.warn("Could not serialize request body for logging", e);
        }

        // 4. Call Msg91 API (use API key directly - no encryption)
        String response = webClient.post()
                .uri(url)
                .header("authkey", config.getApiKeyEncrypted())
                .header("accept", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Template creation failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to create WhatsApp template", error)))
                .block();

        log.info("Template creation response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("raw_response", response);
        }
    }

    /**
     * Build create template request body
     * Transforms WhatsAppCreateTemplateRequest to MSG91 API format
     */
    private Map<String, Object> buildCreateTemplateRequestBody(WhatsAppCreateTemplateRequest request, String integratedNumber) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("integrated_number", integratedNumber);
        body.put("template_name", request.getTemplateName());
        body.put("language", request.getLanguage());
        // MSG91 requires uppercase category: UTILITY, MARKETING, AUTHENTICATION
        String category = request.getCategory() != null ? request.getCategory().toUpperCase() : "UTILITY";
        body.put("category", category);

        if (request.getButtonUrl() != null && request.getButtonUrl()) {
            body.put("button_url", "true");
        }

        // Transform components to MSG91 format
        List<Map<String, Object>> components = new ArrayList<>();
        for (WhatsAppCreateTemplateRequest.TemplateComponent component : request.getComponents()) {
            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("type", component.getType());

            // Handle HEADER component
            if ("HEADER".equals(component.getType())) {
                if (component.getFormat() != null) {
                    comp.put("format", component.getFormat());
                }
                if (component.getText() != null) {
                    comp.put("text", component.getText());
                }
                if (component.getExample() != null) {
                    Map<String, Object> example = new LinkedHashMap<>();
                    if (component.getExample().getHeaderText() != null) {
                        example.put("header_text", component.getExample().getHeaderText());
                    }
                    if (component.getExample().getHeaderHandle() != null) {
                        example.put("header_handle", component.getExample().getHeaderHandle());
                    }
                    if (!example.isEmpty()) {
                        comp.put("example", example);
                    }
                }
            }
            // Handle BODY component
            else if ("BODY".equals(component.getType())) {
                if (component.getText() != null) {
                    comp.put("text", component.getText());
                }
                if (component.getExample() != null && component.getExample().getBodyText() != null) {
                    Map<String, Object> example = new LinkedHashMap<>();
                    example.put("body_text", component.getExample().getBodyText());
                    comp.put("example", example);
                }
            }
            // Handle FOOTER component
            else if ("FOOTER".equals(component.getType())) {
                if (component.getText() != null) {
                    comp.put("text", component.getText());
                }
            }
            // Handle BUTTONS component
            else if ("BUTTONS".equals(component.getType())) {
                if (component.getButtons() != null && !component.getButtons().isEmpty()) {
                    List<Map<String, Object>> buttons = new ArrayList<>();
                    for (WhatsAppCreateTemplateRequest.TemplateButton btn : component.getButtons()) {
                        Map<String, Object> button = new LinkedHashMap<>();
                        button.put("type", btn.getType());
                        button.put("text", btn.getText());

                        if ("URL".equals(btn.getType()) && btn.getUrl() != null) {
                            button.put("url", btn.getUrl());
                            if (btn.getExample() != null) {
                                button.put("example", btn.getExample());
                            }
                        }
                        if ("PHONE_NUMBER".equals(btn.getType()) && btn.getPhoneNumber() != null) {
                            button.put("phone_number", btn.getPhoneNumber());
                        }
                        buttons.add(button);
                    }
                    comp.put("buttons", buttons);
                }
            }

            components.add(comp);
        }

        body.put("components", components);
        return body;
    }

    /**
     * Edit WhatsApp template
     * PUT /api/v5/whatsapp/client-panel-template/{template_id}/
     * integrated_number is read from database config_json
     */
    @SuppressWarnings("null")
    public Map<String, Object> editTemplate(String templateId, WhatsAppEditTemplateRequest request) {
        log.info("Editing WhatsApp template: {}", templateId);

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL and integrated_number from config_json
        String baseUrl = config.getConfigValueAsString("create_template_url");
        String url = baseUrl + templateId + "/";
        String integratedNumber = config.getConfigValueAsString("integrated_number");

        log.info("Using integrated_number: {} (from config_json)", integratedNumber);

        // 3. Build request body with integrated_number from config
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("integrated_number", integratedNumber);
        requestBody.put("components", request.getComponents());
        if (request.getButtonUrl() != null) {
            requestBody.put("button_url", request.getButtonUrl());
        }

        // 4. Call Msg91 API (use API key directly - no encryption)
        String response = webClient.put()
                .uri(url)
                .header("authkey", config.getApiKeyEncrypted())
                .header("Content-Type", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Edit template failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to edit WhatsApp template", error)))
                .block();

        log.info("Edit template response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("raw_response", response);
        }
    }

    /**
     * Delete WhatsApp template
     * DELETE /api/v5/whatsapp/client-panel-template/?integrated_number=X&template_name=Y
     * integrated_number is read from database config_json
     */
    @SuppressWarnings("null")
    public Map<String, Object> deleteTemplate(String templateName) {
        log.info("Deleting WhatsApp template: {}", templateName);

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL and integrated_number from config_json
        String baseUrl = config.getConfigValueAsString("create_template_url");
        String integratedNumber = config.getConfigValueAsString("integrated_number");

        log.info("Using integrated_number: {} (from config_json)", integratedNumber);

        String url = baseUrl + "?integrated_number=" + integratedNumber + "&template_name=" + templateName;

        // 3. Call Msg91 API (use API key directly - no encryption)
        String response = webClient.delete()
                .uri(url)
                .header("authkey", config.getApiKeyEncrypted())
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Delete template failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to delete WhatsApp template", error)))
                .block();

        log.info("Delete template response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("raw_response", response);
        }
    }

    /**
     * Send WhatsApp Payment Link
     * POST /api/v5/whatsapp/whatsapp-outbound-message/
     */
    @SuppressWarnings("null")
    public WhatsAppResponse sendPaymentLink(WhatsAppPaymentLinkRequest request) {
        log.info("Sending WhatsApp payment link to: {}", request.getRecipientNumber());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL from config_json
        String url = config.getConfigValueAsString("single_message_url");

        // 3. Call Msg91 API (use API key directly - no encryption)
        String response = webClient.post()
                .uri(url)
                .header("authkey", config.getApiKeyEncrypted())
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Send payment link failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to send payment link", error)))
                .block();

        log.info("Send payment link response: {}", response);

        // 4. Return response
        return WhatsAppResponse.builder()
                .status("SENT")
                .message("Payment link sent successfully")
                .providerResponse(response)
                .build();
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
        log.info("Building WhatsApp request body for MSG91...");

        // Extract configuration from config_json
        String namespace = config.getConfigValueAsString("namespace");
        String integratedNumber = config.getConfigValueAsString("integrated_number");

        log.info("Extracted from config - namespace: {}, integrated_number: {}", namespace, integratedNumber);

        if (namespace == null || integratedNumber == null) {
            log.error("Missing required config! namespace={}, integrated_number={}", namespace, integratedNumber);
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
        log.info("Template name: {}", request.getTemplateId());

        // Language
        Map<String, String> language = new LinkedHashMap<>();
        if (request.getLanguage() != null) {
            language.put("code", request.getLanguage().getCode());
            language.put("policy", request.getLanguage().getPolicy());
            log.info("Language from request: code={}, policy={}", request.getLanguage().getCode(), request.getLanguage().getPolicy());
        } else {
            // Default language
            language.put("code", "en");
            language.put("policy", "deterministic");
            log.info("Using default language: code=en, policy=deterministic");
        }
        template.put("language", language);

        // Namespace (after language to match curl order)
        template.put("namespace", namespace);

        // Build to_and_components array (single element from simplified input)
        List<Map<String, Object>> toAndComponents = new ArrayList<>();
        Map<String, Object> recipientData = new HashMap<>();
        recipientData.put("to", request.getTo());
        log.info("Recipients (to): {}", request.getTo());

        // Add dynamic components if present
        if (request.getComponents() != null && !request.getComponents().isEmpty()) {
            log.info("Components from request: {}", request.getComponents());
            recipientData.put("components", request.getComponents());
        } else {
            log.warn("WARNING: No components found in request!");
        }

        toAndComponents.add(recipientData);
        log.info("Final to_and_components: {}", toAndComponents);

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
                    .header("authkey", config.getApiKeyEncrypted())
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

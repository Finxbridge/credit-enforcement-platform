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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /**
     * Upload media file to MSG91 for WhatsApp template creation
     * Returns header_handle to use in template HEADER component
     *
     * Database config_json must have:
     * - media_upload_url: URL for media upload API
     * - integrated_number: WhatsApp business number
     */
    @SuppressWarnings("null")
    public WhatsAppMediaUploadResponse uploadMedia(MultipartFile media) {
        log.info("Uploading media to MSG91: filename={}, size={}, contentType={}",
                media.getOriginalFilename(), media.getSize(), media.getContentType());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL and integrated_number from config_json (same pattern as createTemplate)
        String url = config.getConfigValueAsString("media_upload_url");
        String integratedNumber = config.getConfigValueAsString("integrated_number");

        if (url == null || url.isEmpty()) {
            throw new ConfigurationNotFoundException("media_upload_url not configured in database for MSG91_WHATSAPP");
        }

        log.info("Media upload URL: {}, whatsapp_number: {} (from config_json)", url, integratedNumber);

        try {
            // 3. Build multipart request - matching curl exactly:
            // --form 'whatsapp_number="918143170546"'
            // --form 'media=@"/path/to/file.pdf"'
            //
            // MSG91 supported document types: PDF, DOC(X), PPT(X), XLS(X)
            // Important: MSG91 may not recognize long MIME types like
            // "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            // Use application/octet-stream for Office documents and let MSG91 detect from extension
            String contentType = normalizeContentType(media.getContentType(), media.getOriginalFilename());
            String filename = media.getOriginalFilename();

            log.info("Using normalized content-type for upload: {} (original: {})", contentType, media.getContentType());

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("whatsapp_number", integratedNumber);
            builder.part("media", new ByteArrayResource(media.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            }).contentType(MediaType.parseMediaType(contentType));

            // 4. Call MSG91 API
            String response = webClient.post()
                    .uri(url)
                    .header("authkey", config.getApiKeyEncrypted())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Media upload failed", error))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to upload media to MSG91", error)))
                    .block();

            log.info("Media upload response: {}", response);

            // 5. Parse response
            WhatsAppMediaUploadResponse uploadResponse = objectMapper.readValue(response, WhatsAppMediaUploadResponse.class);

            if (uploadResponse.getData() != null && uploadResponse.getData().getUrl() != null) {
                log.info("Media uploaded successfully. header_handle: {}", uploadResponse.getData().getUrl());
            } else {
                log.warn("Media upload response missing URL data: {}", response);
            }

            return uploadResponse;

        } catch (IOException e) {
            log.error("Failed to read media file", e);
            throw new ApiCallException("Failed to read media file", e);
        }
    }

    /**
     * Create WhatsApp template with document in one step
     * 1. Upload document to MSG91 to get header_handle
     * 2. Create template with DOCUMENT header using header_handle
     */
    public Map<String, Object> createTemplateWithDocument(MultipartFile document, WhatsAppCreateTemplateRequest request) {
        log.info("Creating WhatsApp template with document: template={}, file={}",
                request.getTemplateName(), document.getOriginalFilename());

        // Step 1: Upload document to get header_handle
        WhatsAppMediaUploadResponse uploadResponse = uploadMedia(document);

        if (uploadResponse.getData() == null || uploadResponse.getData().getUrl() == null) {
            throw new ApiCallException("Failed to upload document - no header_handle returned");
        }

        String headerHandle = uploadResponse.getData().getUrl();
        log.info("Document uploaded. header_handle: {}", headerHandle);

        // Step 2: Update request with header_handle
        // Find HEADER component and set header_handle
        boolean headerFound = false;
        for (WhatsAppCreateTemplateRequest.TemplateComponent component : request.getComponents()) {
            if ("HEADER".equals(component.getType())) {
                // Set format to DOCUMENT if not set
                if (component.getFormat() == null) {
                    component.setFormat("DOCUMENT");
                }

                // Set header_handle in example
                if (component.getExample() == null) {
                    component.setExample(WhatsAppCreateTemplateRequest.ComponentExample.builder()
                            .headerHandle(List.of(headerHandle))
                            .build());
                } else {
                    component.getExample().setHeaderHandle(List.of(headerHandle));
                }
                headerFound = true;
                break;
            }
        }

        // If no HEADER component exists, add one
        if (!headerFound) {
            List<WhatsAppCreateTemplateRequest.TemplateComponent> components = new ArrayList<>(request.getComponents());
            components.add(0, WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                    .type("HEADER")
                    .format("DOCUMENT")
                    .example(WhatsAppCreateTemplateRequest.ComponentExample.builder()
                            .headerHandle(List.of(headerHandle))
                            .build())
                    .build());
            request.setComponents(components);
        }

        // Step 3: Create template with the updated request
        Map<String, Object> createResponse = createTemplate(request);

        // Add header_handle to response for reference
        createResponse.put("header_handle", headerHandle);

        log.info("Template with document created successfully: {}", createResponse);
        return createResponse;
    }

    // ==================== Helper Methods ====================

    /**
     * Normalize content-type for MSG91 API compatibility
     *
     * MSG91 WhatsApp sample-media-upload API supported types (from documentation):
     * - Documents: PDF, DOC(X), PPT(X), XLS(X) [up to 100MB]
     * - Images: JPG, JPEG, PNG [up to 5MB]
     * - Videos: MP4, 3GPP [up to 16MB]
     *
     * If the API keeps rejecting, the file type may not actually be supported
     * for sample media upload (used during template creation only).
     */
    private String normalizeContentType(String originalContentType, String filename) {
        if (originalContentType == null && filename != null) {
            // Derive content type from filename extension
            return getContentTypeFromFilename(filename);
        }

        if (originalContentType == null) {
            return "application/octet-stream";
        }

        // Return the original content-type - MSG91 should recognize standard MIME types
        return originalContentType;
    }

    /**
     * Get content type from filename extension
     */
    private String getContentTypeFromFilename(String filename) {
        if (filename == null) return "application/octet-stream";

        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".3gpp") || lower.endsWith(".3gp")) return "video/3gpp";

        return "application/octet-stream";
    }

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

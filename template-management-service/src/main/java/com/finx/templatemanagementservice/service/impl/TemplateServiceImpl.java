package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.client.CommunicationServiceClient;
import com.finx.templatemanagementservice.client.DmsServiceClient;
import com.finx.templatemanagementservice.client.WhatsAppMediaUploadClient;
import com.finx.templatemanagementservice.client.dto.DmsDocumentDTO;
import com.finx.templatemanagementservice.domain.dto.*;
import com.finx.templatemanagementservice.domain.dto.comm.EmailTemplateCreateRequest;
import com.finx.templatemanagementservice.domain.dto.comm.SmsAddTemplateVersionRequest;
import com.finx.templatemanagementservice.domain.dto.comm.SmsCreateTemplateRequest;
import com.finx.templatemanagementservice.domain.dto.comm.WhatsAppCreateTemplateRequest;
import com.finx.templatemanagementservice.domain.dto.comm.WhatsAppEditTemplateRequest;
import com.finx.templatemanagementservice.domain.dto.comm.WhatsAppMediaUploadResponse;
import com.finx.templatemanagementservice.domain.entity.Template;
import com.finx.templatemanagementservice.domain.entity.TemplateContent;
import com.finx.templatemanagementservice.domain.entity.TemplateVariable;
import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.exception.BusinessException;
import com.finx.templatemanagementservice.exception.TemplateAlreadyExistsException;
import com.finx.templatemanagementservice.exception.TemplateNotFoundException;
import com.finx.templatemanagementservice.repository.TemplateContentRepository;
import com.finx.templatemanagementservice.repository.TemplateRepository;
import com.finx.templatemanagementservice.repository.TemplateVariableRepository;
import com.finx.templatemanagementservice.service.DocumentProcessingService;
import com.finx.templatemanagementservice.service.FileStorageService;
import com.finx.templatemanagementservice.service.TemplateService;
import com.finx.templatemanagementservice.service.TemplateVariableResolverService;
import com.finx.templatemanagementservice.service.TemplateRenderingService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Template Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateVariableRepository templateVariableRepository;
    private final TemplateContentRepository templateContentRepository;
    private final CommunicationServiceClient communicationServiceClient;
    private final WhatsAppMediaUploadClient whatsAppMediaUploadClient;
    private final DmsServiceClient dmsServiceClient;
    private final FileStorageService fileStorageService;
    private final DocumentProcessingService documentProcessingService;
    private final TemplateVariableResolverService variableResolverService;
    private final TemplateRenderingService renderingService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO createTemplate(CreateTemplateRequest request) {
        log.info("Creating template with code: {}", request.getTemplateCode());

        // Check if template code already exists
        if (templateRepository.existsByTemplateCode(request.getTemplateCode())) {
            throw new TemplateAlreadyExistsException(request.getTemplateCode());
        }

        // Create template entity
        Template template = Template.builder()
                .templateName(request.getTemplateName())
                .templateCode(request.getTemplateCode())
                .channel(request.getChannel())
                .language(request.getLanguage())
                .provider(request.getProvider())
                .providerTemplateId(request.getProviderTemplateId())
                .description(request.getDescription())
                .isActive(true)
                .build();

        // Add variables
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            for (TemplateVariableDTO varDto : request.getVariables()) {
                TemplateVariable variable = TemplateVariable.builder()
                        .variableName(varDto.getVariableName())
                        .variableKey(varDto.getVariableKey())
                        .dataType(varDto.getDataType())
                        .defaultValue(varDto.getDefaultValue())
                        .isRequired(varDto.getIsRequired())
                        .description(varDto.getDescription())
                        .displayOrder(varDto.getDisplayOrder())
                        .build();
                template.addVariable(variable);
            }
        }

        // Add content if provided
        if (request.getContent() != null) {
            TemplateContent content = TemplateContent.builder()
                    .languageCode(request.getContent().getLanguageCode() != null ?
                        request.getContent().getLanguageCode() : "en")
                    .subject(request.getContent().getSubject())
                    .content(request.getContent().getContent())
                    .build();
            template.addContent(content);
        }

        // Save template
        template = templateRepository.save(template);
        log.info("Template created successfully with id: {}", template.getId());

        return mapToDetailDTO(template);
    }

    @Override
    @Cacheable(value = "templates", key = "#id")
    public TemplateDetailDTO getTemplate(Long id) {
        log.info("Fetching template with id: {}", id);
        Template template = templateRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));
        return mapToDetailDTO(template);
    }

    @Override
    @Cacheable(value = "templates", key = "#templateCode")
    public TemplateDetailDTO getTemplateByCode(String templateCode) {
        log.info("Fetching template with code: {}", templateCode);
        Template template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new TemplateNotFoundException("templateCode", templateCode));

        // Fetch variables separately
        List<TemplateVariable> variables = templateVariableRepository
                .findByTemplateIdOrderByDisplayOrder(template.getId());
        template.setVariables(new HashSet<>(variables));

        return mapToDetailDTO(template);
    }

    @Override
    @Cacheable(value = "templates")
    public List<TemplateDTO> getAllTemplates() {
        log.info("Fetching all templates (ordered by latest first)");
        List<Template> templates = templateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return templates.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "templatesByChannel", key = "#channel")
    public List<TemplateDTO> getTemplatesByChannel(ChannelType channel) {
        log.info("Fetching templates for channel: {}", channel);
        List<Template> templates = templateRepository.findByChannelAndIsActiveTrue(channel);
        return templates.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO updateTemplate(Long id, UpdateTemplateRequest request) {
        log.info("Updating template with id: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));

        // Update fields
        if (request.getTemplateName() != null) {
            template.setTemplateName(request.getTemplateName());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getProviderTemplateId() != null) {
            template.setProviderTemplateId(request.getProviderTemplateId());
        }
        if (request.getIsActive() != null) {
            template.setIsActive(request.getIsActive());
        }

        template = templateRepository.save(template);
        log.info("Template updated successfully: {}", id);

        return mapToDetailDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public void deleteTemplate(Long id) {
        log.info("Deleting template with id: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));

        // Soft delete
        template.setIsActive(false);
        templateRepository.save(template);

        log.info("Template deleted successfully: {}", id);
    }

    @Override
    public List<TemplateVariableDTO> getTemplateVariables(Long templateId) {
        log.info("Fetching variables for template: {}", templateId);

        // Verify template exists
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException(templateId);
        }

        List<TemplateVariable> variables = templateVariableRepository
                .findByTemplateIdOrderByDisplayOrder(templateId);

        return variables.stream()
                .map(this::mapVariableToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateDTO> searchTemplates(String keyword) {
        log.info("Searching templates with keyword: {}", keyword);
        List<Template> templates = templateRepository.searchTemplates(keyword);
        return templates.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void syncWithProvider(Long templateId) {
        log.info("Syncing template {} with provider", templateId);

        // Get template with details
        Template template = templateRepository.findByIdWithDetails(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        // Get content for the template
        TemplateContent content = template.getContents() != null && !template.getContents().isEmpty()
                ? template.getContents().iterator().next()
                : null;

        if (content == null) {
            throw new BusinessException("Template content is required for provider sync");
        }

        try {
            switch (template.getChannel()) {
                case SMS -> syncSmsTemplate(template, content);
                case WHATSAPP -> syncWhatsAppTemplate(template, content);
                case EMAIL -> syncEmailTemplate(template, content);
                default -> throw new BusinessException("Unsupported channel: " + template.getChannel());
            }
            log.info("Template {} synced successfully with provider", templateId);
        } catch (Exception e) {
            log.error("Failed to sync template {} with provider: {}", templateId, e.getMessage());
            throw new BusinessException("Failed to sync with provider: " + e.getMessage());
        }
    }

    /**
     * Sync SMS template with MSG91 via communication-service
     */
    private void syncSmsTemplate(Template template, TemplateContent content) {
        log.info("Syncing SMS template: {}", template.getTemplateName());

        // Convert {{variableName}} to ##variableName## for MSG91 format
        String msg91Content = convertToMsg91Format(content.getContent());

        SmsCreateTemplateRequest request = SmsCreateTemplateRequest.builder()
                .templateName(template.getTemplateName())
                .template(msg91Content)
                .dltTemplateId(template.getProviderTemplateId()) // Using providerTemplateId as DLT ID
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createSMSTemplate(request);

        if (response != null && response.getPayload() != null) {
            // Extract provider template ID from response if available
            Object templateId = response.getPayload().get("template_id");
            if (templateId != null && template.getProviderTemplateId() == null) {
                template.setProviderTemplateId(templateId.toString());
                templateRepository.save(template);
            }
            log.info("SMS template synced successfully: {}", response.getPayload());
        }
    }

    /**
     * Sync WhatsApp template with MSG91 via communication-service
     * Handles both templates with and without documents
     */
    private void syncWhatsAppTemplate(Template template, TemplateContent content) {
        log.info("Syncing WhatsApp template: {}", template.getTemplateName());

        // Convert template name to MSG91 format (lowercase with underscores only)
        String msg91TemplateName = convertToMsg91TemplateName(template.getTemplateName());

        // Convert {{variableName}} to {{1}}, {{2}}, etc. for MSG91 WhatsApp format
        // and collect variable names for sample value generation
        List<String> extractedVariables = new ArrayList<>();
        String msg91Content = convertToWhatsAppNumberedFormat(content.getContent(), extractedVariables);

        // Generate sample values based on extracted variable names
        List<String> sampleValues = generateSampleValuesForVariables(extractedVariables);

        // Build components based on template content
        List<WhatsAppCreateTemplateRequest.TemplateComponent> components = new ArrayList<>();

        // Check if template has a document attached - if so, add HEADER component with DOCUMENT format
        String headerHandle = null;
        if (template.getDmsDocumentId() != null && !template.getDmsDocumentId().isEmpty()) {
            log.info("Template has document attached (dmsDocumentId={}). Uploading to MSG91...",
                    template.getDmsDocumentId());
            headerHandle = uploadDocumentToMsg91ForTemplate(template);

            if (headerHandle != null) {
                // Add HEADER component with DOCUMENT format
                WhatsAppCreateTemplateRequest.TemplateComponent headerComponent =
                        WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                                .type("HEADER")
                                .format("DOCUMENT")
                                .example(WhatsAppCreateTemplateRequest.ComponentExample.builder()
                                        .headerHandle(List.of(headerHandle))
                                        .build())
                                .build();
                components.add(headerComponent);
                log.info("Added DOCUMENT header component with header_handle: {}", headerHandle);
            }
        }

        // Build body component with example sample values
        WhatsAppCreateTemplateRequest.TemplateComponent.TemplateComponentBuilder bodyBuilder =
                WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                        .type("BODY")
                        .text(msg91Content);

        // Add example with sample values if variables exist
        if (!sampleValues.isEmpty()) {
            WhatsAppCreateTemplateRequest.ComponentExample example =
                    WhatsAppCreateTemplateRequest.ComponentExample.builder()
                            .bodyText(List.of(sampleValues))
                            .build();
            bodyBuilder.example(example);
        }

        components.add(bodyBuilder.build());

        // Use template's language short code (en_US, hi, te) for MSG91
        String languageCode = template.getLanguage() != null
                ? template.getLanguage().getShortCode()
                : "en_US"; // Default to English

        WhatsAppCreateTemplateRequest request = WhatsAppCreateTemplateRequest.builder()
                .templateName(msg91TemplateName)
                .language(languageCode)
                .category("UTILITY") // Default category
                .components(components)
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createWhatsAppTemplate(request);

        if (response != null && response.getPayload() != null) {
            // MSG91 expects the template NAME (not numeric ID) when sending messages
            // Store the msg91TemplateName we used to create the template as the providerTemplateId
            // This is what we need to use in the "template.name" field when sending WhatsApp messages
            if (template.getProviderTemplateId() == null) {
                template.setProviderTemplateId(msg91TemplateName);
                templateRepository.save(template);
                log.info("Stored MSG91 template name: {} for template: {}", msg91TemplateName, template.getTemplateName());
            }
            log.info("WhatsApp template synced successfully: {}", response.getPayload());
        }
    }

    /**
     * Sync WhatsApp template with document - uses provided document bytes directly
     * This avoids re-downloading the document from DMS
     *
     * @param template The template entity (must have content loaded)
     * @param documentBytes The document file bytes
     * @param filename The original filename
     * @param contentType The content type (e.g., application/pdf)
     */
    private void syncWhatsAppWithDocument(Template template, byte[] documentBytes, String filename, String contentType) {
        log.info("Syncing WhatsApp template with document: {}", template.getTemplateName());

        // Get content from template
        TemplateContent content = template.getContents() != null && !template.getContents().isEmpty()
                ? template.getContents().iterator().next()
                : null;

        if (content == null) {
            throw new BusinessException("Template content is required for WhatsApp sync");
        }

        // Convert template name to MSG91 format (lowercase with underscores only)
        String msg91TemplateName = convertToMsg91TemplateName(template.getTemplateName());

        // Convert {{variableName}} to {{1}}, {{2}}, etc. for MSG91 WhatsApp format
        List<String> extractedVariables = new ArrayList<>();
        String msg91Content = convertToWhatsAppNumberedFormat(content.getContent(), extractedVariables);

        // Generate sample values based on extracted variable names
        List<String> sampleValues = generateSampleValuesForVariables(extractedVariables);

        // Build components based on template content
        List<WhatsAppCreateTemplateRequest.TemplateComponent> components = new ArrayList<>();

        // Upload document to MSG91 to get header_handle
        String headerHandle = null;
        if (documentBytes != null && documentBytes.length > 0) {
            log.info("Uploading document to MSG91: filename={}, size={}, contentType={}",
                    filename, documentBytes.length, contentType);

            CommonResponse<WhatsAppMediaUploadResponse> uploadResponse =
                    whatsAppMediaUploadClient.uploadMedia(documentBytes, filename, contentType);

            if (uploadResponse != null && "success".equals(uploadResponse.getStatus()) && uploadResponse.getPayload() != null) {
                headerHandle = uploadResponse.getPayload().getHeaderHandle();
                log.info("Document uploaded to MSG91. header_handle: {}", headerHandle);
            } else {
                log.error("Failed to upload document to MSG91: {}",
                        uploadResponse != null ? uploadResponse.getMessage() : "null response");
            }
        }

        // Add HEADER component with DOCUMENT format if we got header_handle
        if (headerHandle != null) {
            WhatsAppCreateTemplateRequest.TemplateComponent headerComponent =
                    WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                            .type("HEADER")
                            .format("DOCUMENT")
                            .example(WhatsAppCreateTemplateRequest.ComponentExample.builder()
                                    .headerHandle(List.of(headerHandle))
                                    .build())
                            .build();
            components.add(headerComponent);
            log.info("Added DOCUMENT header component with header_handle");
        }

        // Build body component with example sample values
        WhatsAppCreateTemplateRequest.TemplateComponent.TemplateComponentBuilder bodyBuilder =
                WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                        .type("BODY")
                        .text(msg91Content);

        if (!sampleValues.isEmpty()) {
            WhatsAppCreateTemplateRequest.ComponentExample example =
                    WhatsAppCreateTemplateRequest.ComponentExample.builder()
                            .bodyText(List.of(sampleValues))
                            .build();
            bodyBuilder.example(example);
        }

        components.add(bodyBuilder.build());

        // Use template's language short code
        String languageCode = template.getLanguage() != null
                ? template.getLanguage().getShortCode()
                : "en_US";

        WhatsAppCreateTemplateRequest request = WhatsAppCreateTemplateRequest.builder()
                .templateName(msg91TemplateName)
                .language(languageCode)
                .category("UTILITY")
                .components(components)
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createWhatsAppTemplate(request);

        if (response != null && response.getPayload() != null) {
            if (template.getProviderTemplateId() == null) {
                template.setProviderTemplateId(msg91TemplateName);
                templateRepository.save(template);
                log.info("Stored MSG91 template name: {} for template: {}", msg91TemplateName, template.getTemplateName());
            }
            log.info("WhatsApp template with document synced successfully: {}", response.getPayload());
        }
    }

    /**
     * Upload document from DMS to MSG91 to get header_handle for WhatsApp template
     *
     * @param template The template with dmsDocumentId
     * @return The header_handle from MSG91, or null if upload failed
     */
    private String uploadDocumentToMsg91ForTemplate(Template template) {
        try {
            // Get document content from DMS
            String dmsDocumentId = template.getDmsDocumentId();
            log.info("Fetching document content from DMS: {}", dmsDocumentId);

            // Get document metadata
            CommonResponse<DmsDocumentDTO> docResponse = dmsServiceClient.getDocumentByDocumentId(dmsDocumentId);
            if (docResponse == null || docResponse.getPayload() == null) {
                log.error("Failed to get document metadata from DMS: {}", dmsDocumentId);
                return null;
            }

            DmsDocumentDTO document = docResponse.getPayload();
            String filename = document.getDocumentName() != null ? document.getDocumentName() : "document.pdf";
            String contentType = document.getFileType() != null ? document.getFileType() : "application/pdf";

            // Get document content bytes
            byte[] documentBytes = dmsServiceClient.getDocumentContent(document.getId(), null);
            if (documentBytes == null || documentBytes.length == 0) {
                log.error("Failed to get document content from DMS: {}", dmsDocumentId);
                return null;
            }

            log.info("Document fetched from DMS: filename={}, size={}, contentType={}",
                    filename, documentBytes.length, contentType);

            // Upload to MSG91 via communication-service
            CommonResponse<WhatsAppMediaUploadResponse> uploadResponse =
                    whatsAppMediaUploadClient.uploadMedia(documentBytes, filename, contentType);

            if (uploadResponse != null && "success".equals(uploadResponse.getStatus()) && uploadResponse.getPayload() != null) {
                String headerHandle = uploadResponse.getPayload().getHeaderHandle();
                log.info("Document uploaded to MSG91. header_handle: {}", headerHandle);
                return headerHandle;
            } else {
                log.error("Failed to upload document to MSG91: {}",
                        uploadResponse != null ? uploadResponse.getMessage() : "null response");
                return null;
            }

        } catch (Exception e) {
            log.error("Error uploading document to MSG91 for template: {}", template.getTemplateName(), e);
            return null;
        }
    }

    /**
     * Convert {{variableName}} placeholders to {{1}}, {{2}}, etc. for MSG91 WhatsApp format
     * Also collects the variable names in order for sample value generation
     */
    private String convertToWhatsAppNumberedFormat(String content, List<String> extractedVariables) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Extract all unique variable names in order of appearance
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            // Skip if it's already a number (already in MSG91 format)
            if (!varName.matches("\\d+") && !extractedVariables.contains(varName)) {
                extractedVariables.add(varName);
            }
        }

        // Replace each variable name with its numbered position
        String result = content;
        for (int i = 0; i < extractedVariables.size(); i++) {
            String varName = extractedVariables.get(i);
            // Replace {{variableName}} with {{number}}
            result = result.replace("{{" + varName + "}}", "{{" + (i + 1) + "}}");
        }

        return result;
    }

    /**
     * Generate sample values based on variable names
     * Maps common variable names to appropriate sample values
     */
    private List<String> generateSampleValuesForVariables(List<String> variableNames) {
        List<String> sampleValues = new ArrayList<>();

        for (String varName : variableNames) {
            String sampleValue = getSampleValueForVariable(varName);
            sampleValues.add(sampleValue);
        }

        return sampleValues;
    }

    /**
     * Get appropriate sample value based on variable name
     */
    private String getSampleValueForVariable(String variableName) {
        if (variableName == null) {
            return "Sample value";
        }

        String lowerVar = variableName.toLowerCase();

        // Customer related
        if (lowerVar.contains("customer_name") || lowerVar.equals("name")) {
            return "Naveen Kumar";
        }
        if (lowerVar.contains("customer_first")) {
            return "Naveen";
        }
        if (lowerVar.contains("email")) {
            return "naveen@example.com";
        }
        if (lowerVar.contains("phone") || lowerVar.contains("mobile")) {
            return "9876543210";
        }

        // Loan related
        if (lowerVar.contains("loan_account") || lowerVar.contains("account_number")) {
            return "LA123456789";
        }
        if (lowerVar.contains("outstanding") || lowerVar.contains("amount") || lowerVar.contains("emi")) {
            return "Rs. 25,000";
        }
        if (lowerVar.contains("due_date") || lowerVar.contains("date")) {
            return "15-Jan-2025";
        }
        if (lowerVar.contains("dpd")) {
            return "30";
        }

        // Company related
        if (lowerVar.contains("company_name")) {
            return "FinXBridge";
        }
        if (lowerVar.contains("company_phone")) {
            return "+91-1234567890";
        }
        if (lowerVar.contains("company_email")) {
            return "support@finxbridge.com";
        }

        // Other
        if (lowerVar.contains("payment_link") || lowerVar.contains("link") || lowerVar.contains("url")) {
            return "https://pay.finxbridge.com/abc123";
        }
        if (lowerVar.contains("agent")) {
            return "Agent Rahul";
        }

        // Default
        return "Sample value";
    }

    /**
     * Sync Email template with MSG91 via communication-service
     * Note: Body should be in HTML format, slug is set to same value as name
     */
    private void syncEmailTemplate(Template template, TemplateContent content) {
        log.info("Syncing Email template: {}", template.getTemplateName());

        // Ensure body is HTML formatted - if plain text, wrap in basic HTML
        String htmlBody = ensureHtmlFormat(content.getContent());

        EmailTemplateCreateRequest request = EmailTemplateCreateRequest.builder()
                .name(template.getTemplateName())
                .slug(template.getTemplateName()) // MSG91 requires slug same as name
                .subject(content.getSubject() != null ? content.getSubject() : template.getTemplateName())
                .body(htmlBody)
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createEmailTemplate(request);

        if (response != null && response.getPayload() != null) {
            // Extract provider template ID from response if available
            Object dataObj = response.getPayload().get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                Object templateId = data.get("id");
                if (templateId != null && template.getProviderTemplateId() == null) {
                    template.setProviderTemplateId(templateId.toString());
                    templateRepository.save(template);
                }
            }
            log.info("Email template synced successfully: {}", response.getPayload());
        }
    }

    /**
     * Ensure content is in HTML format for MSG91 email templates
     * If content already has HTML tags, return as-is. Otherwise wrap in basic HTML structure.
     */
    private String ensureHtmlFormat(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Check if already has HTML tags (case-insensitive check for common HTML elements)
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("<html") || lowerContent.contains("<body") ||
            lowerContent.contains("<div") || lowerContent.contains("<p>") ||
            lowerContent.contains("<br") || lowerContent.contains("<table")) {
            // Already HTML formatted
            return content;
        }

        // Wrap plain text in basic HTML structure
        // Convert newlines to <br> tags and wrap in paragraph
        String htmlContent = content
                .replace("\n\n", "</p><p>")
                .replace("\n", "<br>");

        return "<html><body><p>" + htmlContent + "</p></body></html>";
    }

    // ==================== Simplified API Methods ====================

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO createTemplateSimplified(SimpleCreateTemplateRequest request, MultipartFile document) {
        log.info("Creating template (simplified): {} for channel: {}", request.getTemplateName(), request.getChannel());

        // Validate based on channel requirements
        validateTemplateRequest(request, document);

        // Generate template code from name
        String templateCode = generateTemplateCode(request.getTemplateName(), request.getChannel());

        // Check if template code already exists
        if (templateRepository.existsByTemplateCode(templateCode)) {
            throw new TemplateAlreadyExistsException(templateCode);
        }

        // Extract variables from content ({{variableName}} pattern)
        List<String> extractedVariables = extractVariablesFromContent(request.getContent());

        // Determine if this channel requires provider sync
        boolean requiresProviderSync = request.getChannel() == ChannelType.SMS ||
                                       request.getChannel() == ChannelType.WHATSAPP ||
                                       request.getChannel() == ChannelType.EMAIL;

        // Create template entity - initially INACTIVE for channels that require provider sync
        // For NOTICE and IVR channels, set as ACTIVE since they don't need provider sync
        Template template = Template.builder()
                .templateName(request.getTemplateName())
                .templateCode(templateCode)
                .channel(request.getChannel())
                .language(request.getLanguage())
                .providerTemplateId(request.getProviderTemplateId())
                .description(request.getDescription())
                .isActive(!requiresProviderSync) // INACTIVE for SMS/WHATSAPP/EMAIL until synced
                .build();

        // Add extracted variables from content
        int order = 1;
        for (String varName : extractedVariables) {
            TemplateVariable variable = TemplateVariable.builder()
                    .variableName("{{" + varName + "}}")
                    .variableKey(varName)
                    .isRequired(true)
                    .displayOrder(order++)
                    .build();
            template.addVariable(variable);
        }

        // Add content (optional for NOTICE channel)
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            TemplateContent content = TemplateContent.builder()
                    .languageCode("en")
                    .subject(request.getSubject())
                    .content(request.getContent())
                    .build();
            template.addContent(content);
        }

        // Save template (initially INACTIVE for provider-sync channels)
        template = templateRepository.save(template);
        log.info("Template created with id: {} (isActive: {})", template.getId(), template.getIsActive());

        // For WhatsApp with document: Upload document to DMS FIRST before syncing with MSG91
        // This is critical because syncWithProvider needs the dmsDocumentId to upload to MSG91
        byte[] documentBytes = null;
        String documentFilename = null;
        String documentContentType = null;

        if (document != null && !document.isEmpty()) {
            if (request.getChannel() == ChannelType.SMS) {
                log.warn("SMS channel does not support document attachment, ignoring document");
            } else {
                // Upload document to DMS and extract placeholders
                // This sets dmsDocumentId on the template which is needed for WhatsApp document header
                log.info("Uploading document to DMS before provider sync for channel: {}", request.getChannel());
                uploadDocumentToDmsOnly(template, document);
                template = templateRepository.save(template);
                log.info("Document uploaded to DMS with documentId: {}", template.getDmsDocumentId());

                // Store document bytes for WhatsApp MSG91 upload (avoid re-downloading from DMS)
                if (request.getChannel() == ChannelType.WHATSAPP) {
                    try {
                        documentBytes = document.getBytes();
                        documentFilename = document.getOriginalFilename();
                        documentContentType = document.getContentType();
                    } catch (Exception e) {
                        log.warn("Failed to get document bytes for MSG91 upload: {}", e.getMessage());
                    }
                }
            }
        }

        // Auto-sync with provider (SMS, WhatsApp, Email) via communication-service
        // For WhatsApp: If document was uploaded, syncWithProvider will upload to MSG91 and add HEADER component
        if (requiresProviderSync) {
            try {
                // For WhatsApp with document, pass the document bytes directly to avoid re-downloading from DMS
                if (request.getChannel() == ChannelType.WHATSAPP && documentBytes != null) {
                    syncWhatsAppWithDocument(template, documentBytes, documentFilename, documentContentType);
                } else {
                    syncWithProvider(template.getId());
                }
                // Sync successful - mark template as ACTIVE
                template.setIsActive(true);
                template = templateRepository.save(template);
                log.info("Template synced with provider (MSG91) and marked as ACTIVE for channel: {}", request.getChannel());
            } catch (Exception e) {
                log.warn("Failed to sync template with provider: {}. Template remains INACTIVE until sync succeeds.", e.getMessage());
                // Template stays INACTIVE - user needs to retry sync or manually activate after fixing provider issues
            }
        }

        return mapToDetailDTO(template);
    }

    /**
     * Validate template request based on channel requirements
     */
    private void validateTemplateRequest(SimpleCreateTemplateRequest request, MultipartFile document) {
        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        boolean hasDocument = document != null && !document.isEmpty();

        switch (request.getChannel()) {
            case NOTICE:
                // NOTICE channel requires document, content is optional
                if (!hasDocument) {
                    throw new BusinessException("Document is required for NOTICE channel");
                }
                break;
            case SMS:
                // SMS requires content, no document support
                if (!hasContent) {
                    throw new BusinessException("Content is required for SMS channel");
                }
                break;
            case WHATSAPP:
            case EMAIL:
            case IVR:
                // These channels require content
                if (!hasContent) {
                    throw new BusinessException("Content is required for " + request.getChannel() + " channel");
                }
                break;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO updateTemplateSimplified(Long id, SimpleCreateTemplateRequest request, MultipartFile document) {
        log.info("Updating template (simplified): {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));

        // For NOTICE channel, if no new document and no existing document, require document
        boolean hasExistingDocument = template.getDmsDocumentId() != null;
        boolean hasNewDocument = document != null && !document.isEmpty();
        if (request.getChannel() == ChannelType.NOTICE && !hasExistingDocument && !hasNewDocument) {
            throw new BusinessException("Document is required for NOTICE channel");
        }

        // Update basic fields
        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setProviderTemplateId(request.getProviderTemplateId());

        // Extract variables from content
        List<String> extractedVariables = extractVariablesFromContent(request.getContent());

        // Delete existing variables from database first and flush to ensure deletion is committed
        template.getVariables().clear();
        templateVariableRepository.deleteByTemplateId(id);
        entityManager.flush();

        // Add new variables
        int order = 1;
        for (String varName : extractedVariables) {
            TemplateVariable variable = TemplateVariable.builder()
                    .variableName("{{" + varName + "}}")
                    .variableKey(varName)
                    .isRequired(true)
                    .displayOrder(order++)
                    .build();
            template.addVariable(variable);
        }

        // Update content (optional for NOTICE channel)
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            if (template.getContents() != null && !template.getContents().isEmpty()) {
                TemplateContent existingContent = template.getContents().iterator().next();
                existingContent.setSubject(request.getSubject());
                existingContent.setContent(request.getContent());
            } else {
                TemplateContent content = TemplateContent.builder()
                        .languageCode("en")
                        .subject(request.getSubject())
                        .content(request.getContent())
                        .build();
                template.addContent(content);
            }
        }

        template = templateRepository.save(template);

        // Handle document if provided
        if (hasNewDocument) {
            if (request.getChannel() == ChannelType.SMS) {
                log.warn("SMS channel does not support document attachment, ignoring document");
            } else {
                // Upload document and extract placeholders
                return uploadDocumentAndExtractPlaceholders(template.getId(), document);
            }
        }

        // Sync with MSG91 if template has providerTemplateId (already synced before)
        boolean requiresProviderSync = request.getChannel() == ChannelType.SMS
                || request.getChannel() == ChannelType.WHATSAPP
                || request.getChannel() == ChannelType.EMAIL;

        if (requiresProviderSync && template.getProviderTemplateId() != null) {
            try {
                syncUpdateWithProvider(template, request.getContent());
                log.info("Template update synced with provider (MSG91) for channel: {}", request.getChannel());
            } catch (Exception e) {
                log.warn("Failed to sync template update with provider: {}. Local update saved.", e.getMessage());
            }
        }

        log.info("Template updated successfully: {}", id);
        return mapToDetailDTO(template);
    }

    /**
     * Sync template update with MSG91 provider
     */
    private void syncUpdateWithProvider(Template template, String content) {
        log.info("Syncing template update with provider for template: {}", template.getTemplateName());

        switch (template.getChannel()) {
            case WHATSAPP -> syncWhatsAppTemplateUpdate(template, content);
            case SMS -> syncSmsTemplateUpdate(template, content);
            case EMAIL -> syncEmailTemplateUpdate(template, content);
            default -> log.warn("Unsupported channel for provider sync update: {}", template.getChannel());
        }
    }

    /**
     * Sync WhatsApp template update with MSG91
     */
    private void syncWhatsAppTemplateUpdate(Template template, String content) {
        log.info("Syncing WhatsApp template update: {}", template.getTemplateName());

        if (template.getProviderTemplateId() == null || template.getProviderTemplateId().isEmpty()) {
            log.warn("Cannot update WhatsApp template - no providerTemplateId found. Sync with provider first.");
            return;
        }

        // Convert {{variableName}} to {{1}}, {{2}}, etc. for MSG91 WhatsApp format
        List<String> extractedVariables = new ArrayList<>();
        String msg91Content = convertToWhatsAppNumberedFormat(content, extractedVariables);

        // Generate sample values based on extracted variable names
        List<String> sampleValues = generateSampleValuesForVariables(extractedVariables);

        // Build components based on template content
        List<WhatsAppCreateTemplateRequest.TemplateComponent> components = new ArrayList<>();

        // Build body component with example sample values
        WhatsAppCreateTemplateRequest.TemplateComponent.TemplateComponentBuilder bodyBuilder =
                WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                        .type("BODY")
                        .text(msg91Content);

        // Add example with sample values if variables exist
        if (!sampleValues.isEmpty()) {
            WhatsAppCreateTemplateRequest.ComponentExample example =
                    WhatsAppCreateTemplateRequest.ComponentExample.builder()
                            .bodyText(List.of(sampleValues))
                            .build();
            bodyBuilder.example(example);
        }

        components.add(bodyBuilder.build());

        WhatsAppEditTemplateRequest request = WhatsAppEditTemplateRequest.builder()
                .components(components)
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.editWhatsAppTemplate(
                template.getProviderTemplateId(), request);

        if (response != null && response.getPayload() != null) {
            log.info("WhatsApp template update synced successfully: {}", response.getPayload());
        }
    }

    /**
     * Sync SMS template update with MSG91
     * Uses addTemplateVersion API to create a new version of the template
     */
    private void syncSmsTemplateUpdate(Template template, String content) {
        log.info("Syncing SMS template update: {}", template.getTemplateName());

        if (template.getProviderTemplateId() == null || template.getProviderTemplateId().isEmpty()) {
            log.warn("Cannot update SMS template - no providerTemplateId found. Sync with provider first.");
            return;
        }

        // Convert {{variableName}} to ##variableName## for MSG91 format
        String msg91Content = convertToMsg91Format(content);

        SmsAddTemplateVersionRequest request = SmsAddTemplateVersionRequest.builder()
                .templateId(template.getProviderTemplateId())
                .template(msg91Content)
                .dltTemplateId(template.getProviderTemplateId()) // Using providerTemplateId as DLT ID
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.addSMSTemplateVersion(request);

        if (response != null && response.getPayload() != null) {
            log.info("SMS template version added successfully: {}", response.getPayload());
        }
    }

    /**
     * Sync Email template update with MSG91
     * MSG91 doesn't have email edit API - we create a new template
     * Note: Old template is NOT deleted automatically - handled manually if needed
     * Note: slug is set to same value as name (MSG91 requirement)
     */
    private void syncEmailTemplateUpdate(Template template, String content) {
        log.info("Syncing Email template update: {}", template.getTemplateName());

        // Get subject from template content
        String subject = template.getTemplateName(); // Default to template name
        if (template.getContents() != null && !template.getContents().isEmpty()) {
            TemplateContent templateContent = template.getContents().iterator().next();
            if (templateContent.getSubject() != null) {
                subject = templateContent.getSubject();
            }
        }

        // Ensure body is HTML formatted
        String htmlBody = ensureHtmlFormat(content);

        EmailTemplateCreateRequest request = EmailTemplateCreateRequest.builder()
                .name(template.getTemplateName())
                .slug(template.getTemplateName()) // MSG91 requires slug same as name
                .subject(subject)
                .body(htmlBody)
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createEmailTemplate(request);

        if (response != null && response.getPayload() != null) {
            // Extract new template ID from response and update providerTemplateId
            Object dataObj = response.getPayload().get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                Object newTemplateId = data.get("id");
                if (newTemplateId != null) {
                    String oldTemplateId = template.getProviderTemplateId();
                    template.setProviderTemplateId(newTemplateId.toString());
                    templateRepository.save(template);
                    log.info("Email template updated: old={}, new={}", oldTemplateId, newTemplateId);
                }
            }
            log.info("Email template update synced successfully: {}", response.getPayload());
        }
    }

    /**
     * Extract variable names from content using {{variableName}} pattern
     * Returns variables in order of first appearance (for MSG91 body_1, body_2 mapping)
     */
    private List<String> extractVariablesFromContent(String content) {
        List<String> variables = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return variables;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            if (!variables.contains(varName)) {
                variables.add(varName);
            }
        }

        return variables;
    }

    /**
     * Extract variable order from template content for MSG91 body_1, body_2 mapping
     * Variables are extracted in order of first appearance in the content
     */
    private List<String> extractVariableOrder(String content) {
        return extractVariablesFromContent(content);
    }

    /**
     * Convert template variables from {{variableName}} to ##variableName## for MSG91 SMS format
     */
    private String convertToMsg91Format(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        // Replace {{variableName}} with ##variableName##
        return content.replaceAll("\\{\\{([^}]+)\\}\\}", "##$1##");
    }

    /**
     * Convert template name to MSG91 WhatsApp format (lowercase with underscores only)
     * Example: "WhatsApp Payment Reminder" -> "whatsapp_payment_reminder"
     */
    private String convertToMsg91TemplateName(String templateName) {
        if (templateName == null || templateName.isEmpty()) {
            return templateName;
        }
        // Convert to lowercase, replace spaces and special chars with underscores
        return templateName.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", ""); // Remove leading/trailing underscores
    }

    /**
     * Convert template variables from {{variableName}} to {{1}}, {{2}}, etc. for MSG91 WhatsApp format
     * Example: "Hello {{customerName}}, your amount is {{amount}}" -> "Hello {{1}}, your amount is {{2}}"
     */
    private String convertToWhatsAppMsg91Format(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Find all {{variableName}} patterns and replace with {{1}}, {{2}}, etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        StringBuffer result = new StringBuffer();
        int variableIndex = 1;

        while (matcher.find()) {
            matcher.appendReplacement(result, "{{" + variableIndex + "}}");
            variableIndex++;
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Generate template code from name and channel
     * Format: CHANNEL_TEMPLATENAME_TIMESTAMP (max 100 chars)
     */
    private String generateTemplateCode(String templateName, ChannelType channel) {
        String code = templateName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", ""); // Remove leading/trailing underscores

        String channelPrefix = channel.name() + "_";
        String timestamp = "_" + System.currentTimeMillis();

        // Calculate max length for code portion (100 - channel prefix - timestamp)
        int maxCodeLength = 100 - channelPrefix.length() - timestamp.length();

        // Truncate if too long
        if (code.length() > maxCodeLength) {
            code = code.substring(0, maxCodeLength);
        }

        return channelPrefix + code + timestamp;
    }

    /**
     * Upload document to DMS only (without returning DTO)
     * Used during template creation when we need to upload document BEFORE syncing with provider
     * This is critical for WhatsApp templates with documents - MSG91 needs the header_handle from document upload
     *
     * @param template The template entity to update with document info
     * @param document The document file to upload
     */
    private void uploadDocumentToDmsOnly(Template template, MultipartFile document) {
        log.info("Uploading document to DMS only for template: {}", template.getId());

        // Validate document
        validateDocument(document);

        // Generate document name: {templateId}_{channel}_{timestamp}_{originalFilename}
        String channelName = template.getChannel() != null ? template.getChannel().name() : "GENERAL";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String generatedDocName = String.format("%d_%s_%s_%s", template.getId(), channelName, timestamp, document.getOriginalFilename());

        // Upload document to DMS with TEMPLATE category
        CommonResponse<DmsDocumentDTO> dmsResponse = dmsServiceClient.uploadDocumentWithCategory(
                document,
                generatedDocName,
                "TEMPLATE",  // Category: non-generated template document
                channelName, // Channel from template
                null,        // No caseId for templates
                template.getId()  // sourceTemplateId is the template itself
        );

        if (dmsResponse == null || dmsResponse.getPayload() == null) {
            throw new BusinessException("Failed to upload document to DMS");
        }

        DmsDocumentDTO dmsDoc = dmsResponse.getPayload();

        // Update template with DMS document info
        template.setDmsDocumentId(dmsDoc.getDocumentId());
        template.setDocumentUrl(dmsDoc.getFileUrl());
        template.setDocumentStoragePath(dmsDoc.getStoragePath());
        template.setDocumentStorageBucket(dmsDoc.getStorageBucket());
        template.setDocumentOriginalName(dmsDoc.getFileName());
        template.setDocumentType(getDocumentType(dmsDoc.getFileName()));
        template.setDocumentContentType(dmsDoc.getFileType());
        template.setDocumentSizeBytes(dmsDoc.getFileSizeBytes());

        // Extract and store document placeholders
        try {
            // Store temporarily for placeholder extraction
            String tempPath = fileStorageService.uploadFile(document, "temp/" + template.getId());
            List<String> placeholders = documentProcessingService.extractPlaceholders(tempPath);
            template.setHasDocumentVariables(!placeholders.isEmpty());

            // Store placeholders as comma-separated string
            if (!placeholders.isEmpty()) {
                template.setDocumentPlaceholders(String.join(",", placeholders));
                log.info("Extracted {} placeholders from document: {}", placeholders.size(), placeholders);

                // Add document placeholders as template variables too
                int existingVarCount = template.getVariables() != null ? template.getVariables().size() : 0;
                int order = existingVarCount + 1;
                for (String placeholder : placeholders) {
                    // Check if variable already exists
                    boolean exists = template.getVariables().stream()
                            .anyMatch(v -> v.getVariableKey().equals(placeholder));
                    if (!exists) {
                        TemplateVariable variable = TemplateVariable.builder()
                                .variableName("{{" + placeholder + "}}")
                                .variableKey(placeholder)
                                .isRequired(true)
                                .description("From document")
                                .displayOrder(order++)
                                .build();
                        template.addVariable(variable);
                    }
                }
            } else {
                template.setDocumentPlaceholders(null);
            }

            // Clean up temp file
            try {
                fileStorageService.deleteFile(tempPath);
            } catch (Exception e) {
                log.warn("Failed to delete temp file: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to extract document placeholders: {}", e.getMessage());
            template.setHasDocumentVariables(false);
            template.setDocumentPlaceholders(null);
        }

        log.info("Document uploaded to DMS for template: {}, DMS ID: {}", template.getId(), dmsDoc.getDocumentId());
    }

    /**
     * Upload document to DMS and extract placeholders, storing them with the template
     */
    @Transactional
    private TemplateDetailDTO uploadDocumentAndExtractPlaceholders(Long templateId, MultipartFile document) {
        log.info("Uploading document and extracting placeholders for template: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        // Validate document
        validateDocument(document);

        // Delete existing document from DMS if present
        if (template.getDmsDocumentId() != null) {
            try {
                CommonResponse<DmsDocumentDTO> existingDoc = dmsServiceClient.getDocumentByDocumentId(template.getDmsDocumentId());
                if (existingDoc != null && existingDoc.getPayload() != null) {
                    dmsServiceClient.permanentlyDeleteDocument(existingDoc.getPayload().getId());
                    log.info("Deleted existing document from DMS: {}", template.getDmsDocumentId());
                }
            } catch (Exception e) {
                log.warn("Failed to delete existing document from DMS: {}", e.getMessage());
            }
        }

        // Generate document name: {templateId}_{channel}_{timestamp}_{originalFilename}
        String channelName = template.getChannel() != null ? template.getChannel().name() : "GENERAL";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String generatedDocName = String.format("%d_%s_%s_%s", templateId, channelName, timestamp, document.getOriginalFilename());

        // Upload document to DMS with TEMPLATE category
        CommonResponse<DmsDocumentDTO> dmsResponse = dmsServiceClient.uploadDocumentWithCategory(
                document,
                generatedDocName,
                "TEMPLATE",  // Category: non-generated template document
                channelName, // Channel from template
                null,        // No caseId for templates
                templateId   // sourceTemplateId
        );

        if (dmsResponse == null || dmsResponse.getPayload() == null) {
            throw new BusinessException("Failed to upload document to DMS");
        }

        DmsDocumentDTO dmsDoc = dmsResponse.getPayload();

        // Update template with DMS document info
        template.setDmsDocumentId(dmsDoc.getDocumentId());
        template.setDocumentUrl(dmsDoc.getFileUrl());
        template.setDocumentStoragePath(dmsDoc.getStoragePath());
        template.setDocumentStorageBucket(dmsDoc.getStorageBucket());
        template.setDocumentOriginalName(dmsDoc.getFileName());
        template.setDocumentType(getDocumentType(dmsDoc.getFileName()));
        template.setDocumentContentType(dmsDoc.getFileType());
        template.setDocumentSizeBytes(dmsDoc.getFileSizeBytes());

        // Extract and store document placeholders
        List<String> placeholders = Collections.emptyList();
        try {
            // Store temporarily for placeholder extraction
            String tempPath = fileStorageService.uploadFile(document, "temp/" + templateId);
            placeholders = documentProcessingService.extractPlaceholders(tempPath);
            template.setHasDocumentVariables(!placeholders.isEmpty());

            // Store placeholders as JSON array
            if (!placeholders.isEmpty()) {
                template.setDocumentPlaceholders(String.join(",", placeholders));
                log.info("Extracted {} placeholders from document: {}", placeholders.size(), placeholders);

                // Add document placeholders as template variables too
                int existingVarCount = template.getVariables() != null ? template.getVariables().size() : 0;
                int order = existingVarCount + 1;
                for (String placeholder : placeholders) {
                    // Check if variable already exists
                    boolean exists = template.getVariables().stream()
                            .anyMatch(v -> v.getVariableKey().equals(placeholder));
                    if (!exists) {
                        TemplateVariable variable = TemplateVariable.builder()
                                .variableName("{{" + placeholder + "}}")
                                .variableKey(placeholder)
                                .isRequired(true)
                                .description("From document")
                                .displayOrder(order++)
                                .build();
                        template.addVariable(variable);
                    }
                }
            } else {
                template.setDocumentPlaceholders(null);
            }

            // Clean up temp file
            try {
                fileStorageService.deleteFile(tempPath);
            } catch (Exception e) {
                log.warn("Failed to delete temp file: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to extract document placeholders: {}", e.getMessage());
            template.setHasDocumentVariables(false);
            template.setDocumentPlaceholders(null);
        }

        template = templateRepository.save(template);
        log.info("Document uploaded successfully to DMS for template: {}, DMS ID: {}", templateId, dmsDoc.getDocumentId());

        return mapToDetailDTO(template);
    }

    // ==================== Document Management Methods ====================

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO createTemplateWithDocument(CreateTemplateRequest request, MultipartFile document) {
        log.info("Creating template with document: {}", request.getTemplateCode());

        // First create the template
        TemplateDetailDTO createdTemplate = createTemplate(request);

        // Then upload the document
        return uploadDocument(createdTemplate.getId(), document);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO uploadDocument(Long templateId, MultipartFile document) {
        log.info("Uploading document for template: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        // Validate document
        validateDocument(document);

        // Delete existing document from DMS if present
        if (template.getDmsDocumentId() != null) {
            try {
                CommonResponse<DmsDocumentDTO> existingDoc = dmsServiceClient.getDocumentByDocumentId(template.getDmsDocumentId());
                if (existingDoc != null && existingDoc.getPayload() != null) {
                    dmsServiceClient.permanentlyDeleteDocument(existingDoc.getPayload().getId());
                    log.info("Deleted existing document from DMS: {}", template.getDmsDocumentId());
                }
            } catch (Exception e) {
                log.warn("Failed to delete existing document from DMS: {}", e.getMessage());
            }
        }

        // Generate document name: {templateId}_{channel}_{timestamp}_{originalFilename}
        String channelName = template.getChannel() != null ? template.getChannel().name() : "GENERAL";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String generatedDocName = String.format("%d_%s_%s_%s", templateId, channelName, timestamp, document.getOriginalFilename());

        // Upload document to DMS - simple API with just file and documentName
        CommonResponse<DmsDocumentDTO> dmsResponse = dmsServiceClient.uploadDocument(document, generatedDocName);

        if (dmsResponse == null || dmsResponse.getPayload() == null) {
            throw new BusinessException("Failed to upload document to DMS");
        }

        DmsDocumentDTO dmsDoc = dmsResponse.getPayload();

        // Update template with DMS document info
        template.setDmsDocumentId(dmsDoc.getDocumentId());
        template.setDocumentUrl(dmsDoc.getFileUrl());
        template.setDocumentStoragePath(dmsDoc.getStoragePath());
        template.setDocumentStorageBucket(dmsDoc.getStorageBucket());
        template.setDocumentOriginalName(dmsDoc.getFileName());
        template.setDocumentType(getDocumentType(dmsDoc.getFileName()));
        template.setDocumentContentType(dmsDoc.getFileType());
        template.setDocumentSizeBytes(dmsDoc.getFileSizeBytes());

        // Check if document has placeholders
        try {
            String tempPath = fileStorageService.uploadFile(document, "temp/" + templateId);
            boolean hasPlaceholders = documentProcessingService.hasPlaceholders(tempPath);
            template.setHasDocumentVariables(hasPlaceholders);
            // Clean up temp file
            try {
                fileStorageService.deleteFile(tempPath);
            } catch (Exception e) {
                log.warn("Failed to delete temp file: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to check document placeholders: {}", e.getMessage());
            template.setHasDocumentVariables(false);
        }

        template = templateRepository.save(template);
        log.info("Document uploaded successfully to DMS for template: {}, DMS ID: {}", templateId, dmsDoc.getDocumentId());

        return mapToDetailDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "templatesByChannel"}, allEntries = true)
    public TemplateDetailDTO deleteDocument(Long templateId) {
        log.info("Deleting document for template: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        // Delete from DMS if document exists
        if (template.getDmsDocumentId() != null) {
            try {
                CommonResponse<DmsDocumentDTO> existingDoc = dmsServiceClient.getDocumentByDocumentId(template.getDmsDocumentId());
                if (existingDoc != null && existingDoc.getPayload() != null) {
                    dmsServiceClient.permanentlyDeleteDocument(existingDoc.getPayload().getId());
                    log.info("Deleted document from DMS: {}", template.getDmsDocumentId());
                }
            } catch (Exception e) {
                log.warn("Failed to delete document from DMS: {}", e.getMessage());
            }

            template.setDmsDocumentId(null);
            template.setDocumentUrl(null);
            template.setDocumentStoragePath(null);
            template.setDocumentStorageBucket(null);
            template.setDocumentOriginalName(null);
            template.setDocumentType(null);
            template.setDocumentContentType(null);
            template.setDocumentSizeBytes(null);
            template.setHasDocumentVariables(false);

            template = templateRepository.save(template);
        }

        log.info("Document deleted for template: {}", templateId);
        return mapToDetailDTO(template);
    }

    @Override
    public List<String> getDocumentPlaceholders(Long templateId) {
        log.info("Getting document placeholders for template: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        if (template.getDocumentUrl() == null) {
            return Collections.emptyList();
        }

        return documentProcessingService.extractPlaceholders(template.getDocumentUrl());
    }

    @Override
    public TemplateResolveResponse resolveTemplateWithDocument(Long templateId, TemplateResolveRequest request) {
        log.info("Resolving template with document for template: {} and case: {}", templateId, request.getCaseId());

        // Resolve variables from case data
        Map<String, Object> resolvedVariables = variableResolverService.resolveVariablesForTemplate(
                templateId, request.getCaseId(), request.getAdditionalContext()
        );

        // Render template content
        String renderedContent = renderingService.renderTemplateForCase(
                templateId, request.getCaseId(), request.getAdditionalContext()
        );

        // Get template for details
        Template template = templateRepository.findByIdWithDetails(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        String renderedSubject = null;
        String templateContent = null;

        // Render subject if available and extract template content
        if (template.getContents() != null && !template.getContents().isEmpty()) {
            TemplateContent contentEntity = template.getContents().iterator().next();
            templateContent = contentEntity.getContent();
            String subject = contentEntity.getSubject();
            if (subject != null && !subject.isEmpty()) {
                renderedSubject = renderingService.renderSubject(subject, resolvedVariables);
            }
        }

        // Extract variable order from template content (order of appearance for MSG91 body_1, body_2 mapping)
        List<String> variableOrder = extractVariableOrder(templateContent);
        log.info("Extracted variable order for template {}: {}", templateId, variableOrder);

        // Determine header type for WhatsApp templates with document
        // For WhatsApp templates with dmsDocumentId, the header type is DOCUMENT
        // This is used by strategy-engine to build header_1 component for MSG91
        String headerType = null;
        if (template.getChannel() == ChannelType.WHATSAPP && template.getDmsDocumentId() != null) {
            // Determine header type based on document type
            String docType = template.getDocumentType();
            if (docType != null) {
                docType = docType.toUpperCase();
                if (docType.equals("PDF") || docType.equals("DOC") || docType.equals("DOCX")) {
                    headerType = "DOCUMENT";
                } else if (docType.equals("JPG") || docType.equals("JPEG") || docType.equals("PNG") || docType.equals("GIF")) {
                    headerType = "IMAGE";
                } else if (docType.equals("MP4") || docType.equals("AVI") || docType.equals("MOV")) {
                    headerType = "VIDEO";
                }
            } else {
                // Default to DOCUMENT if type is not specified
                headerType = "DOCUMENT";
            }
            log.info("WhatsApp template {} has header type: {}", templateId, headerType);
        }

        // Build response
        TemplateResolveResponse.TemplateResolveResponseBuilder responseBuilder = TemplateResolveResponse.builder()
                .templateId(templateId)
                .templateCode(template.getTemplateCode())
                .providerTemplateId(template.getProviderTemplateId()) // MSG91 template_id - use this for sending messages
                .channel(template.getChannel() != null ? template.getChannel().name() : null)
                .languageShortCode(template.getLanguage() != null ? template.getLanguage().getShortCode() : "En_US")
                .resolvedVariables(resolvedVariables)
                .variableOrder(variableOrder) // Order of variables for MSG91 body_1, body_2 mapping
                .renderedContent(renderedContent)
                .subject(renderedSubject)
                .headerType(headerType) // WhatsApp header type (DOCUMENT, IMAGE, VIDEO)
                .variableCount(template.getVariables() != null ? template.getVariables().size() : 0)
                .resolvedCount(resolvedVariables != null ? resolvedVariables.size() : 0);

        // Process document if exists (only templates with documents hit DMS/S3)
        if (template.getDmsDocumentId() != null && template.getDocumentUrl() != null) {
            responseBuilder
                    .hasDocument(true)
                    .dmsDocumentId(template.getDmsDocumentId())
                    .originalDocumentUrl(template.getDocumentUrl())
                    .documentType(template.getDocumentType())
                    .documentOriginalName(template.getDocumentOriginalName());

            // Always process document for templates with placeholders
            // This downloads from DMS, replaces placeholders, and uploads processed doc back to DMS
            // The processedDocumentUrl is then used when sending via WhatsApp/communication service
            try {
                // Process document and replace placeholders while preserving layout
                // Pass template metadata for proper document categorization (GENERATED category)
                String channelName = template.getChannel() != null ? template.getChannel().name() : null;
                String processedDocumentUrl = documentProcessingService.processDocument(
                        template.getDocumentUrl(),
                        resolvedVariables,
                        request.getCaseId(),
                        template.getId(),  // Source template ID
                        channelName        // Channel name
                );
                responseBuilder.processedDocumentUrl(processedDocumentUrl);
                log.info("Document processed and uploaded to DMS for case: {}. URL: {}",
                        request.getCaseId(), processedDocumentUrl);
            } catch (Exception e) {
                log.error("Failed to process document for case {}: {}", request.getCaseId(), e.getMessage());
                // Continue without processed document - original will be used as fallback
                // This ensures communication still works even if processing fails
            }
        } else {
            responseBuilder.hasDocument(false);
        }

        return responseBuilder.build();
    }

    // ==================== Helper Methods ====================

    private void validateDocument(MultipartFile document) {
        if (document == null || document.isEmpty()) {
            throw new BusinessException("Document file is required");
        }

        String filename = document.getOriginalFilename();
        String extension = getFileExtension(filename);

        if (!isAllowedDocumentType(extension)) {
            throw new BusinessException("Invalid document type. Allowed types: PDF, DOC, DOCX");
        }

        // Max size: 10MB
        long maxSize = 10 * 1024 * 1024;
        if (document.getSize() > maxSize) {
            throw new BusinessException("Document size exceeds maximum allowed (10MB)");
        }
    }

    private boolean isAllowedDocumentType(String extension) {
        return extension != null &&
               (extension.equalsIgnoreCase("pdf") ||
                extension.equalsIgnoreCase("doc") ||
                extension.equalsIgnoreCase("docx"));
    }

    private String getDocumentType(String filename) {
        String extension = getFileExtension(filename);
        if (extension == null) return null;
        return extension.toUpperCase();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // ==================== Mapping Methods ====================

    private TemplateDTO mapToDTO(Template template) {
        return TemplateDTO.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .templateCode(template.getTemplateCode())
                .channel(template.getChannel())
                .language(template.getLanguage())
                .provider(template.getProvider())
                .providerTemplateId(template.getProviderTemplateId())
                .description(template.getDescription())
                .isActive(template.getIsActive())
                .variableCount(template.getVariables() != null ? template.getVariables().size() : 0)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private TemplateDetailDTO mapToDetailDTO(Template template) {
        // Map variables
        List<TemplateVariableDTO> variableDTOs = template.getVariables() != null
                ? template.getVariables().stream()
                        .map(this::mapVariableToDTO)
                        .collect(Collectors.toList())
                : List.of();

        // Map content (get first content or default language)
        TemplateDetailDTO.TemplateContentDTO contentDTO = null;
        if (template.getContents() != null && !template.getContents().isEmpty()) {
            TemplateContent content = template.getContents().iterator().next();
            contentDTO = TemplateDetailDTO.TemplateContentDTO.builder()
                    .languageCode(content.getLanguageCode())
                    .subject(content.getSubject())
                    .content(content.getContent())
                    .build();
        }

        // Get document placeholders from stored field
        List<String> documentPlaceholders = null;
        if (template.getDocumentPlaceholders() != null && !template.getDocumentPlaceholders().isEmpty()) {
            documentPlaceholders = List.of(template.getDocumentPlaceholders().split(","));
        }

        // Get fresh presigned URL from DMS if document exists
        String documentUrl = null;
        if (template.getDmsDocumentId() != null) {
            try {
                CommonResponse<DmsDocumentDTO> dmsResponse = dmsServiceClient.getDocumentByDocumentId(template.getDmsDocumentId());
                if (dmsResponse != null && dmsResponse.getPayload() != null) {
                    Long dmsId = dmsResponse.getPayload().getId();
                    CommonResponse<String> urlResponse = dmsServiceClient.getPresignedUrl(dmsId);
                    if (urlResponse != null && urlResponse.getPayload() != null) {
                        documentUrl = urlResponse.getPayload();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get presigned URL for template {}: {}", template.getId(), e.getMessage());
                // Fall back to stored URL
                documentUrl = template.getDocumentUrl();
            }
        }

        return TemplateDetailDTO.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .templateCode(template.getTemplateCode())
                .channel(template.getChannel())
                .language(template.getLanguage())
                .languageShortCode(template.getLanguage() != null ? template.getLanguage().getShortCode() : null)
                .provider(template.getProvider())
                .providerTemplateId(template.getProviderTemplateId())
                .description(template.getDescription())
                .isActive(template.getIsActive())
                .variables(variableDTOs)
                .content(contentDTO)
                // Document fields (stored in DMS - OVH S3)
                .dmsDocumentId(template.getDmsDocumentId())
                .documentUrl(documentUrl)
                .documentStoragePath(template.getDocumentStoragePath())
                .documentStorageBucket(template.getDocumentStorageBucket())
                .documentOriginalName(template.getDocumentOriginalName())
                .documentType(template.getDocumentType())
                .documentContentType(template.getDocumentContentType())
                .documentSizeBytes(template.getDocumentSizeBytes())
                .hasDocumentVariables(template.getHasDocumentVariables())
                .documentPlaceholders(documentPlaceholders)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private TemplateVariableDTO mapVariableToDTO(TemplateVariable variable) {
        return TemplateVariableDTO.builder()
                .id(variable.getId())
                .variableName(variable.getVariableName())
                .variableKey(variable.getVariableKey())
                .dataType(variable.getDataType())
                .defaultValue(variable.getDefaultValue())
                .isRequired(variable.getIsRequired())
                .description(variable.getDescription())
                .displayOrder(variable.getDisplayOrder())
                .build();
    }
}

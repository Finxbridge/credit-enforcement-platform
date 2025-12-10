package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.client.CommunicationServiceClient;
import com.finx.templatemanagementservice.client.DmsServiceClient;
import com.finx.templatemanagementservice.client.dto.DmsDocumentDTO;
import com.finx.templatemanagementservice.client.dto.DmsUploadRequest;
import com.finx.templatemanagementservice.domain.dto.*;
import com.finx.templatemanagementservice.domain.dto.comm.SmsCreateTemplateRequest;
import com.finx.templatemanagementservice.domain.dto.comm.WhatsAppCreateTemplateRequest;
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
    private final DmsServiceClient dmsServiceClient;
    private final FileStorageService fileStorageService;
    private final DocumentProcessingService documentProcessingService;
    private final TemplateVariableResolverService variableResolverService;
    private final TemplateRenderingService renderingService;

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
        log.info("Fetching all templates");
        List<Template> templates = templateRepository.findByIsActiveTrue();
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

        if (response != null && response.getData() != null) {
            // Extract provider template ID from response if available
            Object templateId = response.getData().get("template_id");
            if (templateId != null && template.getProviderTemplateId() == null) {
                template.setProviderTemplateId(templateId.toString());
                templateRepository.save(template);
            }
            log.info("SMS template synced successfully: {}", response.getData());
        }
    }

    /**
     * Sync WhatsApp template with MSG91 via communication-service
     */
    private void syncWhatsAppTemplate(Template template, TemplateContent content) {
        log.info("Syncing WhatsApp template: {}", template.getTemplateName());

        // Convert template name to MSG91 format (lowercase with underscores only)
        String msg91TemplateName = convertToMsg91TemplateName(template.getTemplateName());

        // Convert {{variableName}} to {{1}}, {{2}}, etc. for MSG91 WhatsApp format
        String msg91Content = convertToWhatsAppMsg91Format(content.getContent());

        // Build components based on template content
        List<WhatsAppCreateTemplateRequest.TemplateComponent> components = new ArrayList<>();

        // Add BODY component with converted template content
        WhatsAppCreateTemplateRequest.TemplateComponent bodyComponent =
                WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                        .type("BODY")
                        .text(msg91Content)
                        .build();
        components.add(bodyComponent);

        WhatsAppCreateTemplateRequest request = WhatsAppCreateTemplateRequest.builder()
                .templateName(msg91TemplateName)
                .language(content.getLanguageCode() != null ? content.getLanguageCode() : "en")
                .category("UTILITY") // Default category
                .components(components)
                .build();

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createWhatsAppTemplate(request);

        if (response != null && response.getData() != null) {
            // Extract provider template ID from response if available
            Object templateId = response.getData().get("id");
            if (templateId != null && template.getProviderTemplateId() == null) {
                template.setProviderTemplateId(templateId.toString());
                templateRepository.save(template);
            }
            log.info("WhatsApp template synced successfully: {}", response.getData());
        }
    }

    /**
     * Sync Email template with MSG91 via communication-service
     */
    private void syncEmailTemplate(Template template, TemplateContent content) {
        log.info("Syncing Email template: {}", template.getTemplateName());

        Map<String, Object> request = Map.of(
                "name", template.getTemplateName(),
                "subject", content.getSubject() != null ? content.getSubject() : template.getTemplateName(),
                "body", content.getContent()
        );

        CommonResponse<Map<String, Object>> response = communicationServiceClient.createEmailTemplate(request);

        if (response != null && response.getData() != null) {
            // Extract provider template ID from response if available
            Object templateId = response.getData().get("id");
            if (templateId != null && template.getProviderTemplateId() == null) {
                template.setProviderTemplateId(templateId.toString());
                templateRepository.save(template);
            }
            log.info("Email template synced successfully: {}", response.getData());
        }
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

        // Create template entity
        Template template = Template.builder()
                .templateName(request.getTemplateName())
                .templateCode(templateCode)
                .channel(request.getChannel())
                .providerTemplateId(request.getProviderTemplateId())
                .description(request.getDescription())
                .isActive(true)
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

        // Save template
        template = templateRepository.save(template);
        log.info("Template created successfully with id: {}", template.getId());

        // Auto-sync with provider (SMS, WhatsApp, Email) via communication-service
        if (request.getChannel() == ChannelType.SMS ||
            request.getChannel() == ChannelType.WHATSAPP ||
            request.getChannel() == ChannelType.EMAIL) {
            try {
                syncWithProvider(template.getId());
                log.info("Template auto-synced with provider (MSG91) for channel: {}", request.getChannel());
            } catch (Exception e) {
                log.warn("Failed to auto-sync template with provider: {}. Template created but not synced with MSG91.", e.getMessage());
                // Don't fail the request - template is created in DB, sync can be retried manually
            }
        }

        // If document is provided and channel supports it
        if (document != null && !document.isEmpty()) {
            if (request.getChannel() == ChannelType.SMS) {
                log.warn("SMS channel does not support document attachment, ignoring document");
            } else {
                // Upload document and extract placeholders
                return uploadDocumentAndExtractPlaceholders(template.getId(), document);
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

        // Extract and update variables from content
        List<String> extractedVariables = extractVariablesFromContent(request.getContent());

        // Clear existing variables and add new ones
        template.getVariables().clear();
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

        log.info("Template updated successfully: {}", id);
        return mapToDetailDTO(template);
    }

    /**
     * Extract variable names from content using {{variableName}} pattern
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
     */
    private String generateTemplateCode(String templateName, ChannelType channel) {
        String code = templateName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_");

        // Truncate if too long
        if (code.length() > 40) {
            code = code.substring(0, 40);
        }

        return channel.name() + "_" + code + "_" + System.currentTimeMillis();
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
                if (existingDoc != null && existingDoc.getData() != null) {
                    dmsServiceClient.permanentlyDeleteDocument(existingDoc.getData().getId());
                    log.info("Deleted existing document from DMS: {}", template.getDmsDocumentId());
                }
            } catch (Exception e) {
                log.warn("Failed to delete existing document from DMS: {}", e.getMessage());
            }
        }

        // Upload document to DMS (OVH S3 storage)
        DmsUploadRequest uploadRequest = DmsUploadRequest.builder()
                .documentType("TEMPLATE_DOC")
                .documentSubtype(getDocumentType(document.getOriginalFilename()))
                .entityType("TEMPLATE")
                .entityId(templateId)
                .documentName(template.getTemplateName() + " - Document")
                .description("Document attachment for template: " + template.getTemplateCode())
                .build();

        CommonResponse<DmsDocumentDTO> dmsResponse = dmsServiceClient.uploadDocument(uploadRequest, document);

        if (dmsResponse == null || dmsResponse.getData() == null) {
            throw new BusinessException("Failed to upload document to DMS");
        }

        DmsDocumentDTO dmsDoc = dmsResponse.getData();

        // Update template with DMS document info
        template.setDmsDocumentId(dmsDoc.getDocumentId());
        template.setDocumentUrl(dmsDoc.getFileUrl());
        template.setDocumentOriginalName(dmsDoc.getFileName());
        template.setDocumentType(getDocumentType(dmsDoc.getFileName()));
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
                if (existingDoc != null && existingDoc.getData() != null) {
                    dmsServiceClient.permanentlyDeleteDocument(existingDoc.getData().getId());
                    log.info("Deleted existing document from DMS: {}", template.getDmsDocumentId());
                }
            } catch (Exception e) {
                log.warn("Failed to delete existing document from DMS: {}", e.getMessage());
            }
        }

        // Upload document to DMS (OVH S3 storage)
        DmsUploadRequest uploadRequest = DmsUploadRequest.builder()
                .documentType("TEMPLATE_DOC")
                .documentSubtype(getDocumentType(document.getOriginalFilename()))
                .entityType("TEMPLATE")
                .entityId(templateId)
                .documentName(template.getTemplateName() + " - Document")
                .description("Document attachment for template: " + template.getTemplateCode())
                .build();

        CommonResponse<DmsDocumentDTO> dmsResponse = dmsServiceClient.uploadDocument(uploadRequest, document);

        if (dmsResponse == null || dmsResponse.getData() == null) {
            throw new BusinessException("Failed to upload document to DMS");
        }

        DmsDocumentDTO dmsDoc = dmsResponse.getData();

        // Update template with DMS document info
        template.setDmsDocumentId(dmsDoc.getDocumentId());
        template.setDocumentUrl(dmsDoc.getFileUrl());
        template.setDocumentOriginalName(dmsDoc.getFileName());
        template.setDocumentType(getDocumentType(dmsDoc.getFileName()));
        template.setDocumentSizeBytes(dmsDoc.getFileSizeBytes());

        // Check if document has placeholders (download content from DMS first)
        try {
            byte[] docContent = dmsServiceClient.getDocumentContent(dmsDoc.getId(), null);
            // Store temporarily for placeholder checking
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
                if (existingDoc != null && existingDoc.getData() != null) {
                    dmsServiceClient.permanentlyDeleteDocument(existingDoc.getData().getId());
                    log.info("Deleted document from DMS: {}", template.getDmsDocumentId());
                }
            } catch (Exception e) {
                log.warn("Failed to delete document from DMS: {}", e.getMessage());
            }

            template.setDmsDocumentId(null);
            template.setDocumentUrl(null);
            template.setDocumentOriginalName(null);
            template.setDocumentType(null);
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

        // Render subject if available
        if (template.getContents() != null && !template.getContents().isEmpty()) {
            String subject = template.getContents().iterator().next().getSubject();
            if (subject != null && !subject.isEmpty()) {
                renderedSubject = renderingService.renderSubject(subject, resolvedVariables);
            }
        }

        // Build response
        TemplateResolveResponse.TemplateResolveResponseBuilder responseBuilder = TemplateResolveResponse.builder()
                .templateId(templateId)
                .templateCode(template.getTemplateCode())
                .channel(template.getChannel() != null ? template.getChannel().name() : null)
                .resolvedVariables(resolvedVariables)
                .renderedContent(renderedContent)
                .subject(renderedSubject)
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

            if (Boolean.TRUE.equals(template.getHasDocumentVariables())) {
                try {
                    // Process document and replace placeholders
                    String processedDocumentUrl = documentProcessingService.processDocument(
                            template.getDocumentUrl(),
                            resolvedVariables,
                            request.getCaseId()
                    );
                    responseBuilder.processedDocumentUrl(processedDocumentUrl);
                    log.info("Document processed successfully for case: {}", request.getCaseId());
                } catch (Exception e) {
                    log.error("Failed to process document: {}", e.getMessage());
                    // Continue without processed document - original is still available
                }
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

        return TemplateDetailDTO.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .templateCode(template.getTemplateCode())
                .channel(template.getChannel())
                .provider(template.getProvider())
                .providerTemplateId(template.getProviderTemplateId())
                .description(template.getDescription())
                .isActive(template.getIsActive())
                .variables(variableDTOs)
                .content(contentDTO)
                // Document fields (stored in DMS)
                .dmsDocumentId(template.getDmsDocumentId())
                .documentUrl(template.getDocumentUrl())
                .documentOriginalName(template.getDocumentOriginalName())
                .documentType(template.getDocumentType())
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

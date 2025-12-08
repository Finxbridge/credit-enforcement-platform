package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.client.CommunicationServiceClient;
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
import com.finx.templatemanagementservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        template.setVariables(variables);

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
                ? template.getContents().get(0)
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

        SmsCreateTemplateRequest request = SmsCreateTemplateRequest.builder()
                .templateName(template.getTemplateName())
                .template(content.getContent())
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

        // Build components based on template content
        List<WhatsAppCreateTemplateRequest.TemplateComponent> components = new ArrayList<>();

        // Add BODY component with template content
        WhatsAppCreateTemplateRequest.TemplateComponent bodyComponent =
                WhatsAppCreateTemplateRequest.TemplateComponent.builder()
                        .type("BODY")
                        .text(content.getContent())
                        .build();
        components.add(bodyComponent);

        WhatsAppCreateTemplateRequest request = WhatsAppCreateTemplateRequest.builder()
                .templateName(template.getTemplateName())
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
            TemplateContent content = template.getContents().get(0);
            contentDTO = TemplateDetailDTO.TemplateContentDTO.builder()
                    .languageCode(content.getLanguageCode())
                    .subject(content.getSubject())
                    .content(content.getContent())
                    .build();
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

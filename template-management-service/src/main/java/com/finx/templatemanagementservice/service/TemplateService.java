package com.finx.templatemanagementservice.service;

import com.finx.templatemanagementservice.domain.dto.*;
import com.finx.templatemanagementservice.domain.enums.ChannelType;

import java.util.List;

/**
 * Service interface for Template management
 */
public interface TemplateService {

    /**
     * Create a new template
     */
    TemplateDetailDTO createTemplate(CreateTemplateRequest request);

    /**
     * Get template by ID
     */
    TemplateDetailDTO getTemplate(Long id);

    /**
     * Get template by code
     */
    TemplateDetailDTO getTemplateByCode(String templateCode);

    /**
     * Get all templates
     */
    List<TemplateDTO> getAllTemplates();

    /**
     * Get templates by channel
     */
    List<TemplateDTO> getTemplatesByChannel(ChannelType channel);

    /**
     * Update template
     */
    TemplateDetailDTO updateTemplate(Long id, UpdateTemplateRequest request);

    /**
     * Delete template
     */
    void deleteTemplate(Long id);

    /**
     * Get template variables
     */
    List<TemplateVariableDTO> getTemplateVariables(Long templateId);

    /**
     * Search templates by keyword
     */
    List<TemplateDTO> searchTemplates(String keyword);

    /**
     * Sync template with communication service (MSG91)
     */
    void syncWithProvider(Long templateId);

    // ==================== Document Management Methods ====================

    /**
     * Create a template with an attached document
     */
    TemplateDetailDTO createTemplateWithDocument(CreateTemplateRequest request, org.springframework.web.multipart.MultipartFile document);

    /**
     * Upload or replace document for an existing template
     */
    TemplateDetailDTO uploadDocument(Long templateId, org.springframework.web.multipart.MultipartFile document);

    /**
     * Delete document from a template
     */
    TemplateDetailDTO deleteDocument(Long templateId);

    /**
     * Get placeholders from template document
     */
    java.util.List<String> getDocumentPlaceholders(Long templateId);

    /**
     * Resolve template with document processing
     * Resolves variables and processes document placeholders if document exists
     */
    TemplateResolveResponse resolveTemplateWithDocument(Long templateId, TemplateResolveRequest request);
}

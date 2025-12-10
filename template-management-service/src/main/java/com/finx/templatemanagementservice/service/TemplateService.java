package com.finx.templatemanagementservice.service;

import com.finx.templatemanagementservice.domain.dto.*;
import com.finx.templatemanagementservice.domain.enums.ChannelType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Template management
 *
 * Simplified API Flow:
 * 1. Frontend selects channel (SMS, WhatsApp, Email, Notice)
 * 2. Frontend shows available variables from VariableDefinitionService
 * 3. User creates template content with {{variableName}} placeholders
 * 4. For non-SMS channels, user can attach a document
 * 5. Document placeholders are auto-extracted and stored with template
 *
 * Channel Requirements:
 * - SMS: content required, no document
 * - WHATSAPP, EMAIL, IVR: content required, optional document
 * - NOTICE: document required, content optional
 */
public interface TemplateService {

    // ==================== Simplified API Methods ====================

    /**
     * Create a template with simplified request
     * Auto-extracts variables from template content ({{variableName}})
     * Auto-extracts and stores document placeholders for non-SMS channels
     *
     * @param request Template creation request
     * @param document Optional document attachment (required for NOTICE channel)
     * @return Created template details
     */
    TemplateDetailDTO createTemplateSimplified(SimpleCreateTemplateRequest request, MultipartFile document);

    /**
     * Update a template with simplified request
     * Optionally handles document attachment
     *
     * @param id Template ID
     * @param request Template update request
     * @param document Optional new document attachment
     * @return Updated template details
     */
    TemplateDetailDTO updateTemplateSimplified(Long id, SimpleCreateTemplateRequest request, MultipartFile document);

    // ==================== Core Methods ====================

    /**
     * Get template by ID
     */
    TemplateDetailDTO getTemplate(Long id);

    /**
     * Get all active templates
     */
    List<TemplateDTO> getAllTemplates();

    /**
     * Get templates by channel
     */
    List<TemplateDTO> getTemplatesByChannel(ChannelType channel);

    /**
     * Soft delete template
     */
    void deleteTemplate(Long id);

    /**
     * Resolve template with document processing
     * Resolves variables and processes document placeholders if document exists
     */
    TemplateResolveResponse resolveTemplateWithDocument(Long templateId, TemplateResolveRequest request);

    // ==================== Internal/Legacy Methods (kept for backward compatibility) ====================

    /**
     * Create a new template (legacy - used internally)
     */
    TemplateDetailDTO createTemplate(CreateTemplateRequest request);

    /**
     * Get template by code (legacy)
     */
    TemplateDetailDTO getTemplateByCode(String templateCode);

    /**
     * Update template (legacy)
     */
    TemplateDetailDTO updateTemplate(Long id, UpdateTemplateRequest request);

    /**
     * Get template variables (legacy)
     */
    List<TemplateVariableDTO> getTemplateVariables(Long templateId);

    /**
     * Search templates by keyword (legacy)
     */
    List<TemplateDTO> searchTemplates(String keyword);

    /**
     * Sync template with communication service (MSG91)
     */
    void syncWithProvider(Long templateId);

    /**
     * Create a template with an attached document (legacy)
     */
    TemplateDetailDTO createTemplateWithDocument(CreateTemplateRequest request, MultipartFile document);

    /**
     * Upload or replace document for an existing template (legacy)
     */
    TemplateDetailDTO uploadDocument(Long templateId, MultipartFile document);

    /**
     * Delete document from a template (legacy)
     */
    TemplateDetailDTO deleteDocument(Long templateId);

    /**
     * Get placeholders from template document
     * Note: Placeholders are now stored with template during creation
     */
    List<String> getDocumentPlaceholders(Long templateId);
}

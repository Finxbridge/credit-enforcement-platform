package com.finx.templatemanagementservice.service;

import java.util.Map;

/**
 * Service for rendering templates with resolved variables
 */
public interface TemplateRenderingService {

    /**
     * Render template content with provided variables
     * Uses Handlebars template engine for placeholder substitution
     *
     * @param templateContent Template content with placeholders (e.g., "Hello {{customer_name}}")
     * @param variables Map of variables to substitute
     * @return Rendered content with substituted values
     */
    String renderTemplate(String templateContent, Map<String, Object> variables);

    /**
     * Render template by ID with case data
     * Fetches template, resolves variables, and renders content
     *
     * @param templateId Template ID
     * @param caseId Case ID for variable resolution
     * @param additionalContext Additional variables to merge
     * @return Rendered content
     */
    String renderTemplateForCase(Long templateId, Long caseId, Map<String, Object> additionalContext);

    /**
     * Render template subject with variables
     *
     * @param subject Subject template with placeholders
     * @param variables Map of variables to substitute
     * @return Rendered subject
     */
    String renderSubject(String subject, Map<String, Object> variables);
}

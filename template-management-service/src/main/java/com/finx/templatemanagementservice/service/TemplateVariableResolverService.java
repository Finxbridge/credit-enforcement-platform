package com.finx.templatemanagementservice.service;

import java.util.Map;

/**
 * Service for resolving template variables from case data
 */
public interface TemplateVariableResolverService {

    /**
     * Resolve all template variables for a given template and case
     * 
     * @param templateId The template ID
     * @param caseId The case ID
     * @param additionalContext Additional context variables
     * @return Map of resolved variable key to value
     */
    Map<String, Object> resolveVariablesForTemplate(Long templateId, Long caseId, Map<String, Object> additionalContext);

    /**
     * Resolve a single variable value from case data
     * 
     * @param variableKey The variable key (e.g., "customer_name")
     * @param caseId The case ID
     * @return Resolved value or null
     */
    Object resolveVariableValue(String variableKey, Long caseId);

    /**
     * Extract value from case entity using entity path
     * 
     * @param caseEntity The case entity
     * @param entityPath The entity path (e.g., "loan.primaryCustomer.fullName")
     * @return Extracted value or null
     */
    Object extractValueFromPath(Object caseEntity, String entityPath);
}

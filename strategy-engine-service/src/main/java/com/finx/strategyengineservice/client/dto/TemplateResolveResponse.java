package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for template variable resolution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResolveResponse {

    /**
     * Template ID
     */
    private Long templateId;

    /**
     * Template code
     */
    private String templateCode;

    /**
     * Communication channel
     */
    private String channel;

    /**
     * Resolved variables (variable name -> value)
     */
    private Map<String, Object> resolvedVariables;

    /**
     * Rendered template content with substituted variables
     */
    private String renderedContent;

    /**
     * Rendered subject (for email templates)
     */
    private String subject;
}

package com.finx.strategyengineservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateResolveResponse {

    /**
     * Template ID
     */
    private Long templateId;

    /**
     * Template code (our internal code)
     */
    private String templateCode;

    /**
     * MSG91 provider template ID - USE THIS for sending messages via MSG91
     * This is the template_id returned by MSG91 during template creation
     */
    private String providerTemplateId;

    /**
     * Communication channel
     */
    private String channel;

    /**
     * Language short code for communication service (En_US, Hi, Te)
     */
    private String languageShortCode;

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

    // Document attachment fields (stored in DMS - OVH S3)
    /**
     * DMS document reference ID - use this to fetch document from DMS service
     */
    private String dmsDocumentId;

    /**
     * Original document URL from DMS (template document)
     */
    private String originalDocumentUrl;

    /**
     * Processed document URL with replaced placeholders
     */
    private String processedDocumentUrl;

    /**
     * Document type (PDF, DOC, DOCX)
     */
    private String documentType;

    /**
     * Original filename of the document
     */
    private String documentOriginalName;

    /**
     * Flag indicating if template has a document attachment
     */
    private Boolean hasDocument;

    /**
     * Total number of variables in template
     */
    private Integer variableCount;

    /**
     * Number of successfully resolved variables
     */
    private Integer resolvedCount;
}

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
}

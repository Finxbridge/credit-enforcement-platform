package com.finx.collectionsservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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
     * Communication channel (WHATSAPP, SMS, EMAIL)
     */
    private String channel;

    /**
     * Language short code for communication service (en_US, hi, te)
     */
    private String languageShortCode;

    /**
     * Resolved variables (variable name -> value)
     */
    private Map<String, Object> resolvedVariables;

    /**
     * Order of variables as they appear in template content
     * Used for MSG91 body_1, body_2, body_3 mapping
     * Example: ["customer_name", "ptp_amount", "ptp_date"]
     * Maps to: body_1 = customer_name value, body_2 = ptp_amount value, etc.
     */
    private List<String> variableOrder;

    /**
     * Rendered template content with substituted variables
     */
    private String renderedContent;

    /**
     * Rendered subject (for email templates)
     */
    private String subject;

    // Document attachment fields
    private String dmsDocumentId;
    private String originalDocumentUrl;
    private String processedDocumentUrl;
    private String documentType;
    private String documentOriginalName;
    private Boolean hasDocument;

    /**
     * WhatsApp header type (DOCUMENT, IMAGE, VIDEO, TEXT, or null if no header)
     */
    private String headerType;

    private Integer variableCount;
    private Integer resolvedCount;
}

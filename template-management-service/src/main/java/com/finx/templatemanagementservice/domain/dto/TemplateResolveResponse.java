package com.finx.templatemanagementservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response containing resolved template variables and rendered content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateResolveResponse {

    private Long templateId;
    private String templateCode;
    private String providerTemplateId; // MSG91 template_id returned during template creation - USE THIS for sending messages
    private String channel; // SMS, WHATSAPP, EMAIL, NOTICE, IVR
    private String languageShortCode; // Language short code for communication service (En_US, Hi, Te)
    private Map<String, Object> resolvedVariables;
    private List<String> variableOrder; // Order of variables as they appear in template content (for MSG91 body_1, body_2 mapping)
    private String renderedContent;
    private String subject; // For email templates
    private Integer variableCount;
    private Integer resolvedCount;

    // Document attachment fields (stored in DMS - OVH S3)
    private String dmsDocumentId;         // DMS document reference ID
    private String originalDocumentUrl;   // Original document URL from DMS
    private String processedDocumentUrl;  // URL of document with replaced placeholders
    private String documentType;          // PDF, DOC, DOCX
    private String documentOriginalName;  // Original filename
    private Boolean hasDocument;

    // WhatsApp header type (DOCUMENT, IMAGE, VIDEO, TEXT, or null if no header)
    // Used by strategy-engine to build header_1 component for MSG91
    private String headerType;
}

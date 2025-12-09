package com.finx.templatemanagementservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Map<String, Object> resolvedVariables;
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
}

package com.finx.templatemanagementservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for uploading documents to DMS service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmsUploadRequest {

    private String documentType;      // TEMPLATE_DOC, TEMPLATE_IMAGE, etc.
    private String documentSubtype;   // PDF, DOCX, PNG, etc.
    private Long categoryId;
    private String entityType;        // TEMPLATE
    private Long entityId;            // Template ID
    private String documentName;
    private String description;
    private Map<String, Object> metadata;
    private List<String> tags;
    private Integer retentionDays;
    private Long createdBy;
}

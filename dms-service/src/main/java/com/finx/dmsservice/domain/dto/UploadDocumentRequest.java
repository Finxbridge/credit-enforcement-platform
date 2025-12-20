package com.finx.dmsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple Upload Document Request DTO
 * Only contains document name - file is passed separately as MultipartFile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequest {

    private String documentName;  // Custom name or original filename

    // Optional categorization fields
    private String documentCategory;  // TEMPLATE, GENERATED, USER_UPLOAD (default: USER_UPLOAD)
    private String channel;           // SMS, EMAIL, WHATSAPP, NOTICE
    private Long caseId;              // For GENERATED documents
    private Long sourceTemplateId;    // For GENERATED documents - link to template
}

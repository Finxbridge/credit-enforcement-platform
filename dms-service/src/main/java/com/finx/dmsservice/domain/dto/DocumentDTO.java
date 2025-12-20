package com.finx.dmsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple Document DTO
 * Contains essential document metadata for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String documentId;      // DMS-generated ID: DOC-YYYYMMDD-XXXXXXXX
    private String documentName;    // Custom name or original filename
    private String fileUrl;         // Full S3 URL
    private String fileName;        // Original filename
    private String fileType;        // MIME type
    private Long fileSizeBytes;
    private String storagePath;     // Path in S3 bucket
    private String storageBucket;   // S3 bucket name
    private String documentStatus;  // ACTIVE, DELETED

    // Document categorization fields
    private String documentCategory;  // TEMPLATE, GENERATED, USER_UPLOAD
    private String channel;           // SMS, EMAIL, WHATSAPP, NOTICE
    private Long caseId;              // For GENERATED documents
    private Long sourceTemplateId;    // For GENERATED documents - link to template

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

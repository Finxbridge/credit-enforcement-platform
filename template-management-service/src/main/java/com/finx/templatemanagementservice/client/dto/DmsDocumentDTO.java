package com.finx.templatemanagementservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple DTO representing document metadata from DMS service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmsDocumentDTO {

    private Long id;
    private String documentId;        // DMS-generated document ID (e.g., DOC-20241208-ABC123)
    private String documentName;      // Custom name or original filename
    private String fileUrl;           // Full S3 URL
    private String fileName;          // Original filename
    private String fileType;          // MIME type
    private Long fileSizeBytes;
    private String storagePath;       // Path in S3 bucket
    private String storageBucket;     // S3 bucket name
    private String documentStatus;    // ACTIVE, DELETED

    // Document categorization fields
    private String documentCategory;  // TEMPLATE, GENERATED, USER_UPLOAD
    private String channel;           // SMS, EMAIL, WHATSAPP, NOTICE
    private Long caseId;              // For GENERATED documents
    private Long sourceTemplateId;    // For GENERATED documents

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

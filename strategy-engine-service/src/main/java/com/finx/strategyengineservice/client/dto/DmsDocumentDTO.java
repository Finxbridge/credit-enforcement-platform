package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing document metadata from DMS service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmsDocumentDTO {

    private Long id;
    private String documentId;        // DMS-generated document ID (e.g., DOC-20241208-ABC123)
    private String documentType;
    private String documentSubtype;
    private Long categoryId;
    private String categoryName;
    private String entityType;
    private Long entityId;
    private String documentName;
    private String description;
    private String fileUrl;           // Public URL to access document
    private String fileName;          // Original filename
    private String fileType;          // MIME type
    private Long fileSizeBytes;
    private String fileHash;
    private String storageProvider;   // S3
    private String storagePath;       // Path in S3 bucket
    private String storageBucket;     // S3 bucket name
    private String documentStatus;
    private Integer versionNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import com.finx.dmsservice.domain.enums.StorageProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String documentId;
    private DocumentType documentType;
    private String documentSubtype;
    private Long categoryId;
    private String categoryName;
    private EntityType entityType;
    private Long entityId;
    private String documentName;
    private String description;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSizeBytes;
    private String fileHash;
    private StorageProvider storageProvider;
    private String storagePath;
    private String storageBucket;
    private String metadata; // JSON string
    private String tags; // JSON string or comma-separated
    private DocumentStatus documentStatus;
    private Boolean isArchived;
    private LocalDateTime archivedAt;
    private Integer versionNumber;
    private Long parentDocumentId;
    private Integer retentionDays;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}

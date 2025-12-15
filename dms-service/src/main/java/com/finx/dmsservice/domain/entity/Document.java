package com.finx.dmsservice.domain.entity;

import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import com.finx.dmsservice.domain.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", unique = true, nullable = false, length = 100)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "document_subtype", length = 50)
    private String documentSubtype;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_hash", length = 255)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", length = 50)
    private StorageProvider storageProvider;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "storage_bucket", length = 100)
    private String storageBucket;

    // Store metadata as JSON string
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Store tags as JSON string (comma-separated or JSON array)
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", length = 20)
    private DocumentStatus documentStatus;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "archived_by")
    private Long archivedBy;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "parent_document_id")
    private Long parentDocumentId;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (documentStatus == null) {
            documentStatus = DocumentStatus.ACTIVE;
        }
        if (isArchived == null) {
            isArchived = false;
        }
        if (versionNumber == null) {
            versionNumber = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

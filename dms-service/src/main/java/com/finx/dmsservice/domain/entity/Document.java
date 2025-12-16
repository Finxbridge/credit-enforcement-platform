package com.finx.dmsservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple Document entity for DMS
 * Stores file metadata and S3 storage location
 */
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
    private String documentId;  // DMS-generated ID: DOC-YYYYMMDD-XXXXXXXX

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;  // Custom name or original filename

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;  // Full S3 URL

    @Column(name = "file_name", length = 255)
    private String fileName;  // Original filename

    @Column(name = "file_type", length = 100)
    private String fileType;  // MIME type

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "storage_path", length = 500)
    private String storagePath;  // Path in S3 bucket

    @Column(name = "storage_bucket", length = 100)
    private String storageBucket;  // S3 bucket name

    @Column(name = "document_status", length = 20)
    private String documentStatus;  // ACTIVE, DELETED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (documentStatus == null) {
            documentStatus = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

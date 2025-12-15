package com.finx.dmsservice.domain.entity;

import com.finx.dmsservice.domain.enums.ExportFormat;
import com.finx.dmsservice.domain.enums.ExportType;
import com.finx.dmsservice.domain.enums.JobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_export_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", unique = true, nullable = false, length = 100)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_type", nullable = false, length = 30)
    private ExportType exportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", nullable = false, length = 20)
    private ExportFormat exportFormat;

    // Store as JSON string
    @Column(name = "filter_criteria", columnDefinition = "TEXT")
    private String filterCriteria;

    // Store as JSON string
    @Column(name = "document_ids", columnDefinition = "TEXT")
    private String documentIds;

    @Column(name = "total_documents")
    private Integer totalDocuments;

    @Column(name = "exported_documents")
    private Integer exportedDocuments;

    @Column(name = "failed_documents")
    private Integer failedDocuments;

    @Column(name = "export_file_url", length = 500)
    private String exportFileUrl;

    @Column(name = "export_file_size_bytes")
    private Long exportFileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", length = 20)
    private JobStatus jobStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (jobStatus == null) {
            jobStatus = JobStatus.PENDING;
        }
        if (totalDocuments == null) {
            totalDocuments = 0;
        }
        if (exportedDocuments == null) {
            exportedDocuments = 0;
        }
        if (failedDocuments == null) {
            failedDocuments = 0;
        }
    }
}

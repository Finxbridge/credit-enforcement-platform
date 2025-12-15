package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.ExportFormat;
import com.finx.dmsservice.domain.enums.ExportType;
import com.finx.dmsservice.domain.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentExportJobDTO {
    private Long id;
    private String jobId;
    private ExportType exportType;
    private ExportFormat exportFormat;
    private String filterCriteria; // JSON string
    private String documentIds; // JSON string
    private Integer totalDocuments;
    private Integer exportedDocuments;
    private Integer failedDocuments;
    private String exportFileUrl;
    private Long exportFileSizeBytes;
    private JobStatus jobStatus;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Long createdBy;
}

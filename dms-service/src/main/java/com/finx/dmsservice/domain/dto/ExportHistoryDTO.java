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
public class ExportHistoryDTO {

    private Long id;
    private String jobId;
    private ExportType exportType;
    private ExportFormat exportFormat;
    private JobStatus status;
    private Integer totalDocuments;
    private Integer exportedDocuments;
    private Integer failedDocuments;
    private Long fileSizeBytes;
    private String fileSizeFormatted;
    private Boolean isDownloaded;
    private Integer downloadCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationSeconds;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
    private LocalDateTime createdAt;
    private Long createdBy;
    private String createdByName;
}

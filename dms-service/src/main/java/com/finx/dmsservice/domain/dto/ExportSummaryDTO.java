package com.finx.dmsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportSummaryDTO {

    // Count by Status
    private Long totalJobs;
    private Long pendingJobs;
    private Long processingJobs;
    private Long completedJobs;
    private Long failedJobs;
    private Long cancelledJobs;

    // Today's Stats
    private Long todayCreated;
    private Long todayCompleted;
    private Long todayFailed;

    // Total Documents
    private Long totalDocumentsExported;
    private Long totalDocumentsFailed;

    // Storage Stats
    private Long totalExportSizeBytes;
    private String totalExportSizeFormatted;

    // Average Duration
    private Long averageDurationSeconds;

    // By Format
    private Map<String, Long> jobsByFormat;

    // By Type
    private Map<String, Long> jobsByType;

    // Active Downloads
    private Long activeDownloads;
    private Long expiredFiles;
}

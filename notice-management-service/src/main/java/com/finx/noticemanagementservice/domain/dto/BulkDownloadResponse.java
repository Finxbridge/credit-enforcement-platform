package com.finx.noticemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDownloadResponse {

    private String downloadId;
    private String downloadUrl;
    private String status; // PROCESSING, READY, FAILED, EXPIRED
    private Integer totalDocuments;
    private Integer processedDocuments;
    private Long fileSizeBytes;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String errorMessage;
}

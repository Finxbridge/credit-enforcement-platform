package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportProgressDTO {

    private String jobId;
    private JobStatus status;
    private Integer totalDocuments;
    private Integer processedDocuments;
    private Integer successCount;
    private Integer failureCount;
    private Double progressPercentage;
    private String currentDocument;
    private LocalDateTime startedAt;
    private LocalDateTime estimatedCompletion;
    private Long elapsedTimeSeconds;
    private Long remainingTimeSeconds;
    private List<ExportError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportError {
        private Long documentId;
        private String documentName;
        private String errorMessage;
        private LocalDateTime timestamp;
    }
}

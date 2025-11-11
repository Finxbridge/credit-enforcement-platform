package com.finx.allocationreallocationservice.domain.dto;

import com.finx.allocationreallocationservice.domain.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailureAnalysisDTO {

    private String batchId;
    private String module;
    private LocalDateTime analysisTimestamp;

    // Overall statistics
    private Integer totalErrors;
    private Integer uniqueCasesAffected;
    private Integer validationErrors;
    private Integer businessRuleErrors;
    private Integer systemErrors;
    private Integer dataIntegrityErrors;
    private Integer dataErrors;
    private Integer processingErrors;

    // Detailed breakdown
    private List<FailureReason> topFailureReasons;
    private List<FieldFailure> fieldFailures;
    private Map<ErrorType, Integer> errorTypeDistribution;
    private List<ErrorDetail> recentErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureReason {
        private String errorMessage;
        private Integer count;
        private Double percentage;
        private ErrorType errorType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldFailure {
        private String fieldName;
        private Integer errorCount;
        private List<String> commonErrors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String errorId;
        private Long caseId;
        private String externalCaseId;
        private Integer rowNumber;
        private ErrorType errorType;
        private String errorMessage;
        private String fieldName;
        private LocalDateTime createdAt;
    }
}

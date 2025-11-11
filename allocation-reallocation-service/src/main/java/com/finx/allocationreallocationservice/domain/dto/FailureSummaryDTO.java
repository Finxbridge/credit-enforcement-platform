package com.finx.allocationreallocationservice.domain.dto;

import com.finx.allocationreallocationservice.domain.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailureSummaryDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalBatches;
    private Integer batchesWithErrors;
    private Integer totalErrors;

    // Error type breakdown
    private ErrorTypeBreakdown errorTypeBreakdown;

    // Module breakdown
    private Map<String, Integer> errorsByModule;

    // Trend data
    private List<DailyErrorCount> dailyErrorTrend;

    // Top failing fields
    private List<FieldErrorSummary> topFailingFields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorTypeBreakdown {
        private Integer validation;
        private Integer businessRule;
        private Integer system;
        private Integer dataIntegrity;
        private Integer dataError;
        private Integer processing;
        private Map<ErrorType, Double> percentages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyErrorCount {
        private LocalDate date;
        private Integer errorCount;
        private Integer batchCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorSummary {
        private String fieldName;
        private Integer errorCount;
        private Integer affectedBatches;
        private String mostCommonError;
    }
}

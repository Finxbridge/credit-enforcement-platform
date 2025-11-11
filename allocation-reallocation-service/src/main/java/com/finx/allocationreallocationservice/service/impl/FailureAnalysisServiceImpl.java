package com.finx.allocationreallocationservice.service.impl;

import com.finx.allocationreallocationservice.domain.dto.FailureAnalysisDTO;
import com.finx.allocationreallocationservice.domain.dto.FailureSummaryDTO;
import com.finx.allocationreallocationservice.domain.entity.BatchError;
import com.finx.allocationreallocationservice.domain.enums.ErrorType;
import com.finx.allocationreallocationservice.repository.BatchErrorRepository;
import com.finx.allocationreallocationservice.service.FailureAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailureAnalysisServiceImpl implements FailureAnalysisService {

    private final BatchErrorRepository batchErrorRepository;

    @Override
    public FailureAnalysisDTO analyzeBatchFailures(String batchId) {
        log.info("Analyzing failures for batch: {}", batchId);

        List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

        if (errors.isEmpty()) {
            return FailureAnalysisDTO.builder()
                    .batchId(batchId)
                    .analysisTimestamp(LocalDateTime.now())
                    .totalErrors(0)
                    .build();
        }

        // Calculate statistics
        Map<ErrorType, Integer> errorTypeDistribution = errors.stream()
                .collect(Collectors.groupingBy(
                        BatchError::getErrorType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Get unique cases affected
        int uniqueCases = (int) errors.stream()
                .filter(e -> e.getCaseId() != null)
                .map(BatchError::getCaseId)
                .distinct()
                .count();

        // Top failure reasons
        Map<String, Long> errorMessageCounts = errors.stream()
                .collect(Collectors.groupingBy(
                        BatchError::getErrorMessage,
                        Collectors.counting()
                ));

        List<FailureAnalysisDTO.FailureReason> topReasons = errorMessageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    String errorMessage = entry.getKey();
                    Integer count = entry.getValue().intValue();
                    Double percentage = (count * 100.0) / errors.size();

                    // Find the error type for this message
                    ErrorType errorType = errors.stream()
                            .filter(e -> e.getErrorMessage().equals(errorMessage))
                            .map(BatchError::getErrorType)
                            .findFirst()
                            .orElse(ErrorType.SYSTEM);

                    return FailureAnalysisDTO.FailureReason.builder()
                            .errorMessage(errorMessage)
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .errorType(errorType)
                            .build();
                })
                .collect(Collectors.toList());

        // Field failures
        Map<String, List<BatchError>> fieldErrors = errors.stream()
                .filter(e -> e.getFieldName() != null)
                .collect(Collectors.groupingBy(BatchError::getFieldName));

        List<FailureAnalysisDTO.FieldFailure> fieldFailures = fieldErrors.entrySet().stream()
                .map(entry -> {
                    List<String> commonErrors = entry.getValue().stream()
                            .map(BatchError::getErrorMessage)
                            .distinct()
                            .limit(5)
                            .collect(Collectors.toList());

                    return FailureAnalysisDTO.FieldFailure.builder()
                            .fieldName(entry.getKey())
                            .errorCount(entry.getValue().size())
                            .commonErrors(commonErrors)
                            .build();
                })
                .sorted(Comparator.comparing(FailureAnalysisDTO.FieldFailure::getErrorCount).reversed())
                .collect(Collectors.toList());

        // Recent errors
        List<FailureAnalysisDTO.ErrorDetail> recentErrors = errors.stream()
                .sorted(Comparator.comparing(BatchError::getCreatedAt).reversed())
                .limit(20)
                .map(error -> FailureAnalysisDTO.ErrorDetail.builder()
                        .errorId(error.getErrorId())
                        .caseId(error.getCaseId())
                        .externalCaseId(error.getExternalCaseId())
                        .rowNumber(error.getRowNumber())
                        .errorType(error.getErrorType())
                        .errorMessage(error.getErrorMessage())
                        .fieldName(error.getFieldName())
                        .createdAt(error.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Get module
        String module = errors.stream()
                .map(BatchError::getModule)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("UNKNOWN");

        return FailureAnalysisDTO.builder()
                .batchId(batchId)
                .module(module)
                .analysisTimestamp(LocalDateTime.now())
                .totalErrors(errors.size())
                .uniqueCasesAffected(uniqueCases)
                .validationErrors(errorTypeDistribution.getOrDefault(ErrorType.VALIDATION, 0))
                .businessRuleErrors(errorTypeDistribution.getOrDefault(ErrorType.BUSINESS_RULE, 0))
                .systemErrors(errorTypeDistribution.getOrDefault(ErrorType.SYSTEM, 0))
                .dataIntegrityErrors(errorTypeDistribution.getOrDefault(ErrorType.DATA_INTEGRITY, 0))
                .dataErrors(errorTypeDistribution.getOrDefault(ErrorType.DATA_ERROR, 0))
                .processingErrors(errorTypeDistribution.getOrDefault(ErrorType.PROCESSING, 0))
                .topFailureReasons(topReasons)
                .fieldFailures(fieldFailures)
                .errorTypeDistribution(errorTypeDistribution)
                .recentErrors(recentErrors)
                .build();
    }

    @Override
    public FailureSummaryDTO getFailureSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Getting failure summary from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<BatchError> errors = batchErrorRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        if (errors.isEmpty()) {
            return FailureSummaryDTO.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalErrors(0)
                    .totalBatches(0)
                    .batchesWithErrors(0)
                    .build();
        }

        // Calculate error type breakdown
        Map<ErrorType, Integer> errorTypeCounts = errors.stream()
                .collect(Collectors.groupingBy(
                        BatchError::getErrorType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        int totalErrors = errors.size();
        Map<ErrorType, Double> percentages = errorTypeCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.round((entry.getValue() * 100.0 / totalErrors) * 100.0) / 100.0
                ));

        FailureSummaryDTO.ErrorTypeBreakdown errorTypeBreakdown = FailureSummaryDTO.ErrorTypeBreakdown.builder()
                .validation(errorTypeCounts.getOrDefault(ErrorType.VALIDATION, 0))
                .businessRule(errorTypeCounts.getOrDefault(ErrorType.BUSINESS_RULE, 0))
                .system(errorTypeCounts.getOrDefault(ErrorType.SYSTEM, 0))
                .dataIntegrity(errorTypeCounts.getOrDefault(ErrorType.DATA_INTEGRITY, 0))
                .dataError(errorTypeCounts.getOrDefault(ErrorType.DATA_ERROR, 0))
                .processing(errorTypeCounts.getOrDefault(ErrorType.PROCESSING, 0))
                .percentages(percentages)
                .build();

        // Module breakdown
        Map<String, Integer> errorsByModule = errors.stream()
                .filter(e -> e.getModule() != null)
                .collect(Collectors.groupingBy(
                        BatchError::getModule,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Get unique batches
        Set<String> uniqueBatches = errors.stream()
                .map(BatchError::getBatchId)
                .collect(Collectors.toSet());

        // Daily error trend
        Map<LocalDate, List<BatchError>> errorsByDate = errors.stream()
                .collect(Collectors.groupingBy(e -> e.getCreatedAt().toLocalDate()));

        List<FailureSummaryDTO.DailyErrorCount> dailyTrend = errorsByDate.entrySet().stream()
                .map(entry -> {
                    Set<String> batchesOnDate = entry.getValue().stream()
                            .map(BatchError::getBatchId)
                            .collect(Collectors.toSet());

                    return FailureSummaryDTO.DailyErrorCount.builder()
                            .date(entry.getKey())
                            .errorCount(entry.getValue().size())
                            .batchCount(batchesOnDate.size())
                            .build();
                })
                .sorted(Comparator.comparing(FailureSummaryDTO.DailyErrorCount::getDate))
                .collect(Collectors.toList());

        // Top failing fields
        Map<String, List<BatchError>> fieldErrors = errors.stream()
                .filter(e -> e.getFieldName() != null)
                .collect(Collectors.groupingBy(BatchError::getFieldName));

        List<FailureSummaryDTO.FieldErrorSummary> topFailingFields = fieldErrors.entrySet().stream()
                .map(entry -> {
                    Set<String> affectedBatches = entry.getValue().stream()
                            .map(BatchError::getBatchId)
                            .collect(Collectors.toSet());

                    String mostCommonError = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                    BatchError::getErrorMessage,
                                    Collectors.counting()
                            ))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Unknown");

                    return FailureSummaryDTO.FieldErrorSummary.builder()
                            .fieldName(entry.getKey())
                            .errorCount(entry.getValue().size())
                            .affectedBatches(affectedBatches.size())
                            .mostCommonError(mostCommonError)
                            .build();
                })
                .sorted(Comparator.comparing(FailureSummaryDTO.FieldErrorSummary::getErrorCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return FailureSummaryDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalBatches(uniqueBatches.size())
                .batchesWithErrors(uniqueBatches.size())
                .totalErrors(totalErrors)
                .errorTypeBreakdown(errorTypeBreakdown)
                .errorsByModule(errorsByModule)
                .dailyErrorTrend(dailyTrend)
                .topFailingFields(topFailingFields)
                .build();
    }

    @Override
    public List<FailureAnalysisDTO.FailureReason> getTopFailureReasons(int limit) {
        log.info("Getting top {} failure reasons", limit);

        List<BatchError> allErrors = batchErrorRepository.findAll();

        if (allErrors.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Long> errorMessageCounts = allErrors.stream()
                .collect(Collectors.groupingBy(
                        BatchError::getErrorMessage,
                        Collectors.counting()
                ));

        return errorMessageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    String errorMessage = entry.getKey();
                    Integer count = entry.getValue().intValue();
                    Double percentage = (count * 100.0) / allErrors.size();

                    ErrorType errorType = allErrors.stream()
                            .filter(e -> e.getErrorMessage().equals(errorMessage))
                            .map(BatchError::getErrorType)
                            .findFirst()
                            .orElse(ErrorType.SYSTEM);

                    return FailureAnalysisDTO.FailureReason.builder()
                            .errorMessage(errorMessage)
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .errorType(errorType)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public FailureSummaryDTO.ErrorTypeBreakdown getFailuresByErrorType(String batchId) {
        log.info("Getting failures by error type for batch: {}", batchId);

        List<BatchError> errors = (batchId != null)
                ? batchErrorRepository.findByBatchId(batchId)
                : batchErrorRepository.findAll();

        if (errors.isEmpty()) {
            return FailureSummaryDTO.ErrorTypeBreakdown.builder()
                    .validation(0)
                    .businessRule(0)
                    .system(0)
                    .dataIntegrity(0)
                    .dataError(0)
                    .processing(0)
                    .build();
        }

        Map<ErrorType, Integer> errorTypeCounts = errors.stream()
                .collect(Collectors.groupingBy(
                        BatchError::getErrorType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        int totalErrors = errors.size();
        Map<ErrorType, Double> percentages = errorTypeCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.round((entry.getValue() * 100.0 / totalErrors) * 100.0) / 100.0
                ));

        return FailureSummaryDTO.ErrorTypeBreakdown.builder()
                .validation(errorTypeCounts.getOrDefault(ErrorType.VALIDATION, 0))
                .businessRule(errorTypeCounts.getOrDefault(ErrorType.BUSINESS_RULE, 0))
                .system(errorTypeCounts.getOrDefault(ErrorType.SYSTEM, 0))
                .dataIntegrity(errorTypeCounts.getOrDefault(ErrorType.DATA_INTEGRITY, 0))
                .dataError(errorTypeCounts.getOrDefault(ErrorType.DATA_ERROR, 0))
                .processing(errorTypeCounts.getOrDefault(ErrorType.PROCESSING, 0))
                .percentages(percentages)
                .build();
    }

    @Override
    public List<FailureAnalysisDTO.FieldFailure> getFailuresByField(String batchId) {
        log.info("Getting failures by field for batch: {}", batchId);

        List<BatchError> errors = (batchId != null)
                ? batchErrorRepository.findByBatchId(batchId)
                : batchErrorRepository.findAll();

        if (errors.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<BatchError>> fieldErrors = errors.stream()
                .filter(e -> e.getFieldName() != null)
                .collect(Collectors.groupingBy(BatchError::getFieldName));

        return fieldErrors.entrySet().stream()
                .map(entry -> {
                    List<String> commonErrors = entry.getValue().stream()
                            .map(BatchError::getErrorMessage)
                            .distinct()
                            .limit(5)
                            .collect(Collectors.toList());

                    return FailureAnalysisDTO.FieldFailure.builder()
                            .fieldName(entry.getKey())
                            .errorCount(entry.getValue().size())
                            .commonErrors(commonErrors)
                            .build();
                })
                .sorted(Comparator.comparing(FailureAnalysisDTO.FieldFailure::getErrorCount).reversed())
                .collect(Collectors.toList());
    }
}

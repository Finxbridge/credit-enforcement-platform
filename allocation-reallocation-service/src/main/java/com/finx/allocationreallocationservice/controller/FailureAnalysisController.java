package com.finx.allocationreallocationservice.controller;

import com.finx.allocationreallocationservice.domain.dto.CommonResponse;
import com.finx.allocationreallocationservice.domain.dto.FailureAnalysisDTO;
import com.finx.allocationreallocationservice.domain.dto.FailureSummaryDTO;
import com.finx.allocationreallocationservice.service.FailureAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/failure-analysis")
@RequiredArgsConstructor
@Tag(name = "Failure Analysis", description = "APIs for analyzing allocation/reallocation failures")
public class FailureAnalysisController {

    private final FailureAnalysisService failureAnalysisService;

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get comprehensive failure analysis for a specific batch")
    public ResponseEntity<CommonResponse<FailureAnalysisDTO>> analyzeBatchFailures(
            @PathVariable String batchId) {
        log.info("Analyzing batch failures for: {}", batchId);

        FailureAnalysisDTO analysis = failureAnalysisService.analyzeBatchFailures(batchId);

        return ResponseEntity.ok(CommonResponse.success(
                "Failure analysis completed successfully",
                analysis
        ));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get failure summary across date range")
    public ResponseEntity<CommonResponse<FailureSummaryDTO>> getFailureSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting failure summary from {} to {}", startDate, endDate);

        FailureSummaryDTO summary = failureAnalysisService.getFailureSummary(startDate, endDate);

        return ResponseEntity.ok(CommonResponse.success(
                "Failure summary retrieved successfully",
                summary
        ));
    }

    @GetMapping("/top-reasons")
    @Operation(summary = "Get top failure reasons across all batches")
    public ResponseEntity<CommonResponse<List<FailureAnalysisDTO.FailureReason>>> getTopFailureReasons(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting top {} failure reasons", limit);

        List<FailureAnalysisDTO.FailureReason> topReasons = failureAnalysisService.getTopFailureReasons(limit);

        return ResponseEntity.ok(CommonResponse.success(
                "Top failure reasons retrieved successfully",
                topReasons
        ));
    }

    @GetMapping("/by-error-type")
    @Operation(summary = "Get failures grouped by error type")
    public ResponseEntity<CommonResponse<FailureSummaryDTO.ErrorTypeBreakdown>> getFailuresByErrorType(
            @RequestParam(required = false) String batchId) {
        log.info("Getting failures by error type" + (batchId != null ? " for batch: " + batchId : ""));

        FailureSummaryDTO.ErrorTypeBreakdown breakdown = failureAnalysisService.getFailuresByErrorType(batchId);

        return ResponseEntity.ok(CommonResponse.success(
                "Error type breakdown retrieved successfully",
                breakdown
        ));
    }

    @GetMapping("/by-field")
    @Operation(summary = "Get failures grouped by field name")
    public ResponseEntity<CommonResponse<List<FailureAnalysisDTO.FieldFailure>>> getFailuresByField(
            @RequestParam(required = false) String batchId) {
        log.info("Getting failures by field" + (batchId != null ? " for batch: " + batchId : ""));

        List<FailureAnalysisDTO.FieldFailure> fieldFailures = failureAnalysisService.getFailuresByField(batchId);

        return ResponseEntity.ok(CommonResponse.success(
                "Field failures retrieved successfully",
                fieldFailures
        ));
    }
}

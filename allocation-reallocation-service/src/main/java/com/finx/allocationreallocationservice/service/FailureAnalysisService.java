package com.finx.allocationreallocationservice.service;

import com.finx.allocationreallocationservice.domain.dto.FailureAnalysisDTO;
import com.finx.allocationreallocationservice.domain.dto.FailureSummaryDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for analyzing and reporting on allocation/reallocation failures
 */
public interface FailureAnalysisService {

    /**
     * Get comprehensive failure analysis for a specific batch
     * @param batchId The batch ID to analyze
     * @return Detailed failure analysis
     */
    FailureAnalysisDTO analyzeBatchFailures(String batchId);

    /**
     * Get failure summary across all batches within a date range
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Summary of failures by type, module, and field
     */
    FailureSummaryDTO getFailureSummary(LocalDate startDate, LocalDate endDate);

    /**
     * Get top failure reasons across all batches
     * @param limit Number of top reasons to return
     * @return List of most common failure reasons with counts
     */
    List<FailureAnalysisDTO.FailureReason> getTopFailureReasons(int limit);

    /**
     * Get failures grouped by error type
     * @param batchId Optional batch ID filter
     * @return Failures grouped by error type
     */
    FailureSummaryDTO.ErrorTypeBreakdown getFailuresByErrorType(String batchId);

    /**
     * Get failures grouped by field name
     * @param batchId Optional batch ID filter
     * @return Failures grouped by field name
     */
    List<FailureAnalysisDTO.FieldFailure> getFailuresByField(String batchId);
}

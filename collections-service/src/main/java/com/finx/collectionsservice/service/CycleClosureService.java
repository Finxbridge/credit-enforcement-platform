package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.CaseClosureRequest;
import com.finx.collectionsservice.domain.dto.CaseClosureResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Cycle Closure management
 */
public interface CycleClosureService {

    /**
     * Close a single case
     */
    CaseClosureResponse closeCase(Long caseId, String closureReason);

    /**
     * Close multiple cases in bulk
     */
    CaseClosureResponse closeCasesBulk(List<Long> caseIds, String closureReason);

    /**
     * Reopen a closed case
     */
    CaseClosureResponse reopenCase(Long caseId);

    /**
     * Get closed cases
     */
    Page<CaseClosureResponse> getClosedCases(Pageable pageable);

    /**
     * Get closure history for a case
     */
    List<CaseClosureResponse> getCaseClosureHistory(Long caseId);
}

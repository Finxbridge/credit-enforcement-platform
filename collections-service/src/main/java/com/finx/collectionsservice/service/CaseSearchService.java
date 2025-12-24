package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.OTSCaseSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for searching cases for OTS creation
 */
public interface CaseSearchService {

    /**
     * Search cases by query string (customer name, loan account, case number, mobile)
     */
    Page<OTSCaseSearchDTO> searchCases(String query, Pageable pageable);

    /**
     * Get case details by ID for OTS form auto-population
     */
    OTSCaseSearchDTO getCaseDetails(Long caseId);
}

package com.finx.collectionsservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for template variable resolution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResolveRequest {

    /**
     * Case ID for resolving variables from case data
     */
    private Long caseId;

    /**
     * Additional context variables to merge with resolved variables
     * Use this for PTP-specific data like ptpAmount, ptpDate, etc.
     */
    private Map<String, Object> additionalContext;
}

package com.finx.strategyengineservice.client.dto;

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
     * Case ID for resolving variables
     */
    private Long caseId;

    /**
     * Additional context variables to merge
     */
    private Map<String, Object> additionalContext;
}

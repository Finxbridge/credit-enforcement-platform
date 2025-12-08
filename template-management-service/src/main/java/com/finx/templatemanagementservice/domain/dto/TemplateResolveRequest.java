package com.finx.templatemanagementservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to resolve template variables for a specific case
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResolveRequest {

    @NotNull(message = "Template ID is required")
    private Long templateId;

    @NotNull(message = "Case ID is required")
    private Long caseId;

    // Optional: Additional context data that's not in case entity
    private Map<String, Object> additionalContext;
}

package com.finx.templatemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Template Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateRequest {

    private String templateName;
    private String description;
    private String providerTemplateId;
    private Boolean isActive;
}

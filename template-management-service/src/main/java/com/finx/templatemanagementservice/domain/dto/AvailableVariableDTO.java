package com.finx.templatemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified DTO for available variables response
 * Only contains essential fields for template creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableVariableDTO {

    /**
     * Variable key to use in templates (e.g., customer_name, loan_account_number)
     * Use this in template content like {{customer_name}}
     */
    private String variableKey;

    /**
     * Example value for reference
     */
    private String exampleValue;

    /**
     * Whether the variable is active
     */
    private Boolean isActive;
}

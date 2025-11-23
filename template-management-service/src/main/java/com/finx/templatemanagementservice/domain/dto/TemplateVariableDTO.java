package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Template Variable DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVariableDTO {

    private Long id;

    @NotBlank(message = "Variable name is required")
    private String variableName; // VAR1, VAR2, body_1, header_1

    @NotBlank(message = "Variable key is required")
    private String variableKey; // customer_name, loan_account

    private DataType dataType;

    private String defaultValue;

    private Boolean isRequired;

    private String description;

    private Integer displayOrder;
}

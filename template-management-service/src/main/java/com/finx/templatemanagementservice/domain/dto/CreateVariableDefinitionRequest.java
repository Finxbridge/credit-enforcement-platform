package com.finx.templatemanagementservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariableDefinitionRequest {

    @NotBlank(message = "Variable key is required")
    @Pattern(regexp = "^[a-z_][a-z0-9_]*$", message = "Variable key must be lowercase with underscores (e.g., customer_name)")
    private String variableKey;

    @NotBlank(message = "Display name is required")
    private String displayName;

    @NotBlank(message = "Entity path is required")
    private String entityPath;

    @NotBlank(message = "Data type is required")
    private String dataType; // TEXT, NUMBER, DATE, CURRENCY, BOOLEAN, EMAIL, PHONE

    private String defaultValue;
    private String transformer; // DATE_DDMMYYYY, CURRENCY_INR, UPPERCASE, CAPITALIZE, LOWERCASE
    private String description;
    private String category; // CUSTOMER, LOAN, PAYMENT, CASE, DATES, COMPANY
    private String exampleValue;
}

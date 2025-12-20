package com.finx.templatemanagementservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariableDefinitionDTO {

    private Long id;
    private String variableKey;
    private String displayName;
    private String entityPath;
    private String dataType;
    private String defaultValue;
    private String transformer;
    private String description;
    private String category;
    private String exampleValue;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

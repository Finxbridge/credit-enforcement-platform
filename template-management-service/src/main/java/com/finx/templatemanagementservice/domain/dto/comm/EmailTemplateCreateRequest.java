package com.finx.templatemanagementservice.domain.dto.comm;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email Template Creation Request for MSG91
 * Also used for "edit" since MSG91 doesn't have email edit API
 * For edit: create new template, then delete old one
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailTemplateCreateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String slug;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;
}

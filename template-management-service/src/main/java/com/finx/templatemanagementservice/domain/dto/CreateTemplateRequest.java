package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.domain.enums.LanguageType;
import com.finx.templatemanagementservice.domain.enums.ProviderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create Template Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotBlank(message = "Template code is required")
    private String templateCode;

    @NotNull(message = "Channel is required")
    private ChannelType channel;

    @NotNull(message = "Language is required")
    private LanguageType language;

    private ProviderType provider;

    private String providerTemplateId; // MSG91 template ID

    private String description;

    @NotEmpty(message = "At least one variable is required")
    @Valid
    private List<TemplateVariableDTO> variables;

    private TemplateContentDTO content;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateContentDTO {
        private String languageCode;
        private String subject; // For email
        private String content;
    }
}

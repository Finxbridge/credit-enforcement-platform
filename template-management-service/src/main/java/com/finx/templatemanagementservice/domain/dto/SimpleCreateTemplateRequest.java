package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified Create Template Request DTO
 * Frontend sends template text with variables like {{customerName}}, {{loanAccount}}
 * Backend auto-extracts variables from the template content
 *
 * Note: For NOTICE channel, content is optional (only document is required)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleCreateTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotNull(message = "Channel is required")
    private ChannelType channel;  // SMS, WHATSAPP, EMAIL, NOTICE, IVR

    /**
     * Template text with {{variableName}} placeholders
     * Required for SMS, WHATSAPP, EMAIL, IVR channels
     * Optional for NOTICE channel (only document required)
     */
    private String content;

    private String subject;  // For EMAIL channel only

    private String description;

    private String providerTemplateId;  // MSG91 template ID (optional)
}

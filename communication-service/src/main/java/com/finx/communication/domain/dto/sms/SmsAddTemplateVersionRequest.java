package com.finx.communication.domain.dto.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to add a new version to an existing SMS template in Msg91
 * Note: senderId is read from database config_json
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAddTemplateVersionRequest {

    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotBlank(message = "Template content is required")
    private String template;

    @NotBlank(message = "DLT template ID is required")
    private String dltTemplateId;
}

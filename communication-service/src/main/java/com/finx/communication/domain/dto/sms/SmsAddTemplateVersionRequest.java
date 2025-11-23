package com.finx.communication.domain.dto.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to add a new version to an existing SMS template in Msg91
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAddTemplateVersionRequest {

    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Template content is required")
    private String template;

    @NotBlank(message = "DLT template ID is required")
    private String dltTemplateId;
}

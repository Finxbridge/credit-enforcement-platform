package com.finx.communication.domain.dto.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create SMS template in Msg91
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCreateTemplateRequest {

    @NotBlank(message = "Template content is required")
    private String template;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotBlank(message = "DLT template ID is required")
    private String dltTemplateId;

    private String smsType; // NORMAL, PROMOTIONAL, TRANSACTIONAL
}

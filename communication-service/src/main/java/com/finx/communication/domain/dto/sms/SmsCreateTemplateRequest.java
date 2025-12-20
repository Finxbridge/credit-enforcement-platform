package com.finx.communication.domain.dto.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create SMS template in Msg91
 * Note: smsType and senderId are read from database config_json
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCreateTemplateRequest {

    @NotBlank(message = "Template content is required")
    private String template;

    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotBlank(message = "DLT template ID is required")
    private String dltTemplateId;
}

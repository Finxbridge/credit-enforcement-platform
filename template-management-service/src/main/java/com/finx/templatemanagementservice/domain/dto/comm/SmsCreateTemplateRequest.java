package com.finx.templatemanagementservice.domain.dto.comm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create SMS template in Msg91 via communication-service
 * Note: smsType and senderId are read from database config_json in communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCreateTemplateRequest {

    private String template;

    private String templateName;

    private String dltTemplateId;
}

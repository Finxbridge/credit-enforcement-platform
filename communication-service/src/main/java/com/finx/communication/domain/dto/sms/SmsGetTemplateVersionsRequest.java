package com.finx.communication.domain.dto.sms;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to get SMS template versions from Msg91
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsGetTemplateVersionsRequest {

    @NotBlank(message = "Template ID is required")
    @JsonProperty("template_id")
    private String templateId;
}

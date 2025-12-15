package com.finx.templatemanagementservice.domain.dto.comm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WhatsApp Edit Template Request for MSG91
 * PUT /api/v5/whatsapp/client-panel-template/{template_id}/
 * Note: integrated_number is read from database config_json in communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppEditTemplateRequest {

    private List<WhatsAppCreateTemplateRequest.TemplateComponent> components;

    @JsonProperty("button_url")
    private Boolean buttonUrl;
}

package com.finx.communication.domain.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WhatsApp Edit Template Request for MSG91
 * PUT /api/v5/whatsapp/client-panel-template/{template_id}/
 * Note: integrated_number is read from database config_json
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppEditTemplateRequest {

    private List<WhatsAppComponent> components;

    @JsonProperty("button_url")
    private Boolean buttonUrl;
}

package com.finx.communication.domain.dto.voice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MSG91 Voice SMS Request
 * POST /api/v5/voice/call/
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceSmsRequest {

    @NotBlank(message = "Template is required")
    private String template;

    @NotBlank(message = "Caller ID is required")
    @JsonProperty("caller_id")
    private String callerId;

    @NotBlank(message = "Client number is required")
    @JsonProperty("client_number")
    private String clientNumber;

    @JsonProperty("callback_url")
    private String callbackUrl;

    private Map<String, VariableConfig> variables;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VariableConfig {
        private String type;

        @JsonProperty("as_digits")
        private Boolean asDigits;

        private String value;

        @JsonProperty("currency_code")
        private String currencyCode;
    }
}

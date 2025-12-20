package com.finx.communication.domain.dto.voice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MSG91 Voice API Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceResponse {

    private String status;
    private String message;
    private String callId;
    private List<String> messageIds;
    private String providerResponse;
}

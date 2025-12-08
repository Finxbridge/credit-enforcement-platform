package com.finx.communication.domain.dto.voice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MSG91 Voice Click to Call Request
 * POST /api/v5/voice/call/ctc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceClickToCallRequest {

    @NotBlank(message = "Caller ID is required")
    @JsonProperty("caller_id")
    private String callerId;

    @NotBlank(message = "Destination is required")
    private String destination;

    @JsonProperty("destinationB")
    private List<String> destinationB;
}

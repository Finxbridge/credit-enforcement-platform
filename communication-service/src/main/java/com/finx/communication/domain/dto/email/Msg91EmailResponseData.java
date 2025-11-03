package com.finx.communication.domain.dto.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MSG91 Email Response Data DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg91EmailResponseData {

    @JsonProperty("thread_id")
    private Integer threadId;

    @JsonProperty("unique_id")
    private String uniqueId;

    @JsonProperty("message_id")
    private String messageId;
}

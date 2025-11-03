package com.finx.communication.domain.dto.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Msg91EmailResponse {

    private String status;
    private Data data;
    private Map<String, Object> errors;
    @JsonProperty("hasError")
    private boolean hasError;
    private String message;

    @lombok.Data
    public static class Data {
        @JsonProperty("unique_id")
        private String uniqueId;
    }
}
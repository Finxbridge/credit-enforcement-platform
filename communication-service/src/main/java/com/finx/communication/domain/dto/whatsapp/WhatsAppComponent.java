package com.finx.communication.domain.dto.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * WhatsApp message component (header, body, etc.)
 * Supports dynamic parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppComponent {

    private String type; // header, body, footer, button
    private String value;

    /**
     * Dynamic parameters for this component
     * Example: header_1, body_1, body_2, etc.
     */
    private Map<String, ComponentParam> params;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentParam {
        private String type; // text, image, video, document
        private String value;
    }
}

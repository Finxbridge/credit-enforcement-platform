package com.finx.communication.domain.dto.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp recipient with dynamic components
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppRecipient {

    private List<String> to; // List of mobile numbers with country code

    /**
     * Dynamic components for this recipient
     * Example: {"header_1": {"type": "image", "value": "url"}, "body_1": {"type": "text", "value": "John"}}
     */
    private Map<String, Map<String, String>> components;
}

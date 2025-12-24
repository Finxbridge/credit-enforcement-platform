package com.finx.collectionsservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO from Communication Service WhatsApp API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendResponse {

    private String status;

    private List<String> messageIds;

    private String message;

    private Map<String, Object> providerResponse;
}

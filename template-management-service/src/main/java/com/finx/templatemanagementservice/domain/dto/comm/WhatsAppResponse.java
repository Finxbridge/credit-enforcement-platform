package com.finx.templatemanagementservice.domain.dto.comm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WhatsApp Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppResponse {

    private String status;
    private String message;
    private List<String> messageIds;
    private String providerResponse;
}

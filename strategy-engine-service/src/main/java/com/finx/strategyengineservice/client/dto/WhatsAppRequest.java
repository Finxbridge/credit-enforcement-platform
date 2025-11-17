package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WhatsApp Request DTO for communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppRequest {
    private String mobile;
    private String message;
    private String templateId;
    private Long caseId;
    private String caseNumber;
}

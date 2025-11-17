package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SMS Request DTO for communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSRequest {
    private String mobile;
    private String message;
    private String templateId;
    private Long caseId;
    private String caseNumber;
}

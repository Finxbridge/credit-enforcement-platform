package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email Request DTO for communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String email;
    private String subject;
    private String body;
    private String templateId;
    private Long caseId;
    private String caseNumber;
}

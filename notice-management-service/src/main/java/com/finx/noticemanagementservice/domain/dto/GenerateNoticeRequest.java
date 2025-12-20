package com.finx.noticemanagementservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateNoticeRequest {

    @NotNull(message = "Notice ID is required")
    private Long noticeId;

    @NotNull(message = "Template ID is required")
    private Long templateId;

    private Map<String, Object> templateVariables;

    private Long generatedBy;
}

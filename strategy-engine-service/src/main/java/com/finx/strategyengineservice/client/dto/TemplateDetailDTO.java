package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Template Detail DTO for Feign Client communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetailDTO {

    private Long id;
    private String templateName;
    private String templateCode;
    private String channel;
    private String provider;
    private String providerTemplateId;
    private String description;
    private Boolean isActive;
    private List<TemplateVariableDTO> variables;
    private TemplateContentDTO content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateVariableDTO {
        private Long id;
        private String variableName;
        private String variableKey;
        private String dataType;
        private String defaultValue;
        private Boolean isRequired;
        private String description;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateContentDTO {
        private String languageCode;
        private String subject;
        private String content;
    }
}

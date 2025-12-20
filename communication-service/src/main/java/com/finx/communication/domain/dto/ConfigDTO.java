package com.finx.communication.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System Configuration DTO
 * Used for sharing system configuration across services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDTO {

    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String dataType;
    private Boolean isActive;
}

package com.finx.masterdataservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MasterDataDTO {
    private Long id;
    private String dataType;
    private String code;
    private String value;
    private String parentCode;
    private Integer displayOrder;
    private Boolean isActive;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

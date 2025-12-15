package com.finx.dmsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCategoryDTO {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String description;
    private List<String> allowedFileTypes;
    private Integer maxFileSizeMb;
    private Integer retentionDays;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

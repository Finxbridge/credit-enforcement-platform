package com.finx.dmsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Category code is required")
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private Long parentCategoryId;

    private String description;

    private List<String> allowedFileTypes;

    private Integer maxFileSizeMb;

    private Integer retentionDays;

    private Integer displayOrder;
}

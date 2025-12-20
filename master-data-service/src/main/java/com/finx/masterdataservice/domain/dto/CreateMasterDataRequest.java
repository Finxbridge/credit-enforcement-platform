package com.finx.masterdataservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateMasterDataRequest {

    @NotBlank(message = "Category type is required")
    private String categoryType;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Value is required")
    private String value;

    private Integer displayOrder;

    private Boolean isActive;
}

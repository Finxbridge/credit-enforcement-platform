package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseClosureRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotBlank(message = "Closure reason is required")
    @Size(max = 100, message = "Closure reason must not exceed 100 characters")
    private String closureReason;
}

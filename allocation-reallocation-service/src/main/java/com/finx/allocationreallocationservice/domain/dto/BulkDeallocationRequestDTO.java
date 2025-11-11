package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeallocationRequestDTO {
    @NotEmpty(message = "Case IDs list cannot be empty")
    private List<Long> caseIds;

    @NotNull(message = "Reason is required")
    private String reason;
}

package com.finx.allocationreallocationservice.domain.dto;

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
public class ReallocationByFilterRequestDTO {
    @NotNull(message = "filterCriteria is required")
    private Map<String, Object> filterCriteria;

    @NotNull(message = "toUserId is required")
    private Long toUserId;

    private String reason;
}

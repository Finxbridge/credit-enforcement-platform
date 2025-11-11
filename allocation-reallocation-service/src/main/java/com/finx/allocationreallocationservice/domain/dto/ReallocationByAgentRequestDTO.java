package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationByAgentRequestDTO {
    @NotNull(message = "fromUserId is required")
    private Long fromUserId;

    @NotNull(message = "toUserId is required")
    private Long toUserId;

    private String reason;
}

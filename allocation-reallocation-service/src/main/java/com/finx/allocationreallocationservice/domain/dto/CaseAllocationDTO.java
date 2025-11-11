package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAllocationDTO {
    private Long caseId;
    private AgentDTO primaryAgent;
    private AgentDTO secondaryAgent;
    private LocalDateTime allocatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentDTO {
        private Long userId;
        private String username;
    }
}

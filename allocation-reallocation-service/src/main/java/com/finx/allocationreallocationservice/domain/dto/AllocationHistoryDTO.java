package com.finx.allocationreallocationservice.domain.dto;

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
public class AllocationHistoryDTO {
    private Long caseId;
    private List<HistoryItemDTO> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItemDTO {
        private Long allocatedToUserId;
        private String allocatedToUsername;
        private LocalDateTime allocatedAt;
        private String action;
        private String reason;
    }
}

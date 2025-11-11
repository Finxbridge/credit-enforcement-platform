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
public class ErrorDTO {
    private String errorId;
    private String module;
    private String type;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private List<AffectedRecordDTO> affectedRecords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedRecordDTO {
        private Long caseId;
        private String externalId;
    }
}

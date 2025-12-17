package com.finx.collectionsservice.domain.dto;

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
public class CaseClosureResponse {

    private int totalRequested;
    private int totalClosed;
    private int totalFailed;
    private String closureReason;
    private LocalDateTime closedAt;
    private List<Long> closedCaseIds;
    private List<Long> failedCaseIds;
    private String message;
}

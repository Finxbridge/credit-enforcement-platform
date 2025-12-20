package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsSyncHistoryDTO {
    private Long id;
    private Long lmsId;
    private String lmsCode;
    private String lmsName;
    private String syncType;
    private SyncStatus syncStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationSeconds;
    private Integer totalRecords;
    private Integer newRecords;
    private Integer updatedRecords;
    private Integer failedRecords;
    private Integer skippedRecords;
    private String errorMessage;
    private Map<String, Object> errorDetails;
    private String syncBatchId;
    private String triggeredBy;
    private Long triggeredByUser;
    private LocalDateTime createdAt;
}

package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.AuthType;
import com.finx.configurationsservice.domain.enums.LmsType;
import com.finx.configurationsservice.domain.enums.SyncFrequency;
import com.finx.configurationsservice.domain.enums.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsConfigurationDTO {
    private Long id;
    private Long organizationId;
    private String lmsCode;
    private String lmsName;
    private LmsType lmsType;
    private String description;
    private String connectionUrl;
    private String databaseName;
    private String schemaName;
    private String apiEndpoint;
    private AuthType apiAuthType;
    private String fileLocation;
    private String fileFormat;
    private String fileDelimiter;
    private String fileEncoding;
    private SyncFrequency syncFrequency;
    private String syncSchedule;
    private LocalTime syncStartTime;
    private Integer batchSize;
    private Map<String, String> fieldMappings;
    private String lmsIdentifier;
    private String paymentTypeId;
    private List<String> productTypes;
    private Boolean isActive;
    private LocalDateTime lastSyncAt;
    private SyncStatus lastSyncStatus;
    private String lastSyncMessage;
    private Integer lastSyncRecords;
    private Integer lastSyncDurationSeconds;
    private Long totalRecordsSynced;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}

package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.entity.LmsConfiguration;
import com.finx.configurationsservice.domain.entity.LmsSyncHistory;
import com.finx.configurationsservice.domain.enums.LmsType;
import com.finx.configurationsservice.domain.enums.SyncStatus;
import com.finx.configurationsservice.domain.enums.TestStatus;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.repository.LmsConfigurationRepository;
import com.finx.configurationsservice.repository.LmsSyncHistoryRepository;
import com.finx.configurationsservice.service.AuditLogService;
import com.finx.configurationsservice.service.LmsConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LmsConfigurationServiceImpl implements LmsConfigurationService {

    private final LmsConfigurationRepository lmsConfigRepository;
    private final LmsSyncHistoryRepository syncHistoryRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LmsConfigurationDTO createLmsConfig(CreateLmsConfigRequest request) {
        if (lmsConfigRepository.existsByLmsCode(request.getLmsCode())) {
            throw new BusinessException("LMS configuration with code '" + request.getLmsCode() + "' already exists");
        }

        LmsConfiguration lmsConfig = LmsConfiguration.builder()
                .lmsCode(request.getLmsCode())
                .lmsName(request.getLmsName())
                .lmsType(request.getLmsType())
                .description(request.getDescription())
                .connectionUrl(request.getConnectionUrl())
                .databaseName(request.getDatabaseName())
                .schemaName(request.getSchemaName())
                .username(request.getUsername())
                .password(request.getPassword())
                .apiEndpoint(request.getApiEndpoint())
                .apiAuthType(request.getApiAuthType())
                .apiKey(request.getApiKey())
                .apiSecret(request.getApiSecret())
                .fileLocation(request.getFileLocation())
                .fileFormat(request.getFileFormat())
                .fileDelimiter(request.getFileDelimiter())
                .fileEncoding(request.getFileEncoding())
                .syncFrequency(request.getSyncFrequency())
                .syncSchedule(request.getSyncSchedule())
                .syncStartTime(request.getSyncStartTime())
                .batchSize(request.getBatchSize())
                .fieldMappings(request.getFieldMappings())
                .lmsIdentifier(request.getLmsIdentifier())
                .paymentTypeId(request.getPaymentTypeId())
                .productTypes(request.getProductTypes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        LmsConfiguration saved = lmsConfigRepository.save(lmsConfig);

        auditLogService.logCreate("LmsConfiguration", saved.getId().toString(), saved.getLmsName(),
                convertToMap(saved));

        log.info("Created LMS configuration: {} ({})", saved.getLmsName(), saved.getLmsCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LmsConfigurationDTO getLmsConfigById(Long id) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with id: " + id));
        return mapToDTO(lmsConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public LmsConfigurationDTO getLmsConfigByCode(String code) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findByLmsCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with code: " + code));
        return mapToDTO(lmsConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LmsConfigurationDTO> getLmsConfigsByType(LmsType type) {
        return lmsConfigRepository.findByLmsType(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LmsConfigurationDTO> getActiveLmsConfigs() {
        return lmsConfigRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LmsConfigurationDTO> getActiveLmsConfigsByType(LmsType type) {
        return lmsConfigRepository.findByLmsTypeAndIsActiveTrue(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LmsConfigurationDTO updateLmsConfig(Long id, UpdateLmsConfigRequest request) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with id: " + id));

        Map<String, Object> oldValue = convertToMap(lmsConfig);
        List<String> changedFields = new ArrayList<>();

        if (request.getLmsName() != null) {
            lmsConfig.setLmsName(request.getLmsName());
            changedFields.add("lmsName");
        }
        if (request.getLmsType() != null) {
            lmsConfig.setLmsType(request.getLmsType());
            changedFields.add("lmsType");
        }
        if (request.getDescription() != null) {
            lmsConfig.setDescription(request.getDescription());
            changedFields.add("description");
        }
        if (request.getConnectionUrl() != null) {
            lmsConfig.setConnectionUrl(request.getConnectionUrl());
            changedFields.add("connectionUrl");
        }
        if (request.getDatabaseName() != null) {
            lmsConfig.setDatabaseName(request.getDatabaseName());
            changedFields.add("databaseName");
        }
        if (request.getSchemaName() != null) {
            lmsConfig.setSchemaName(request.getSchemaName());
            changedFields.add("schemaName");
        }
        if (request.getUsername() != null) {
            lmsConfig.setUsername(request.getUsername());
            changedFields.add("username");
        }
        if (request.getPassword() != null) {
            lmsConfig.setPassword(request.getPassword());
            changedFields.add("password");
        }
        if (request.getApiEndpoint() != null) {
            lmsConfig.setApiEndpoint(request.getApiEndpoint());
            changedFields.add("apiEndpoint");
        }
        if (request.getApiAuthType() != null) {
            lmsConfig.setApiAuthType(request.getApiAuthType());
            changedFields.add("apiAuthType");
        }
        if (request.getApiKey() != null) {
            lmsConfig.setApiKey(request.getApiKey());
            changedFields.add("apiKey");
        }
        if (request.getApiSecret() != null) {
            lmsConfig.setApiSecret(request.getApiSecret());
            changedFields.add("apiSecret");
        }
        if (request.getFileLocation() != null) {
            lmsConfig.setFileLocation(request.getFileLocation());
            changedFields.add("fileLocation");
        }
        if (request.getFileFormat() != null) {
            lmsConfig.setFileFormat(request.getFileFormat());
            changedFields.add("fileFormat");
        }
        if (request.getFileDelimiter() != null) {
            lmsConfig.setFileDelimiter(request.getFileDelimiter());
            changedFields.add("fileDelimiter");
        }
        if (request.getFileEncoding() != null) {
            lmsConfig.setFileEncoding(request.getFileEncoding());
            changedFields.add("fileEncoding");
        }
        if (request.getSyncFrequency() != null) {
            lmsConfig.setSyncFrequency(request.getSyncFrequency());
            changedFields.add("syncFrequency");
        }
        if (request.getSyncSchedule() != null) {
            lmsConfig.setSyncSchedule(request.getSyncSchedule());
            changedFields.add("syncSchedule");
        }
        if (request.getSyncStartTime() != null) {
            lmsConfig.setSyncStartTime(request.getSyncStartTime());
            changedFields.add("syncStartTime");
        }
        if (request.getBatchSize() != null) {
            lmsConfig.setBatchSize(request.getBatchSize());
            changedFields.add("batchSize");
        }
        if (request.getFieldMappings() != null) {
            lmsConfig.setFieldMappings(request.getFieldMappings());
            changedFields.add("fieldMappings");
        }
        if (request.getLmsIdentifier() != null) {
            lmsConfig.setLmsIdentifier(request.getLmsIdentifier());
            changedFields.add("lmsIdentifier");
        }
        if (request.getPaymentTypeId() != null) {
            lmsConfig.setPaymentTypeId(request.getPaymentTypeId());
            changedFields.add("paymentTypeId");
        }
        if (request.getProductTypes() != null) {
            lmsConfig.setProductTypes(request.getProductTypes());
            changedFields.add("productTypes");
        }
        if (request.getIsActive() != null) {
            lmsConfig.setIsActive(request.getIsActive());
            changedFields.add("isActive");
        }

        LmsConfiguration saved = lmsConfigRepository.save(lmsConfig);

        auditLogService.logUpdate("LmsConfiguration", saved.getId().toString(), saved.getLmsName(),
                oldValue, convertToMap(saved), changedFields);

        log.info("Updated LMS configuration: {} ({})", saved.getLmsName(), saved.getLmsCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deactivateLmsConfig(Long id) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with id: " + id));

        Map<String, Object> oldValue = convertToMap(lmsConfig);
        lmsConfig.setIsActive(false);
        lmsConfigRepository.save(lmsConfig);

        auditLogService.logUpdate("LmsConfiguration", lmsConfig.getId().toString(), lmsConfig.getLmsName(),
                oldValue, convertToMap(lmsConfig), List.of("isActive"));

        log.info("Deactivated LMS configuration: {} ({})", lmsConfig.getLmsName(), lmsConfig.getLmsCode());
    }

    @Override
    @Transactional
    public void activateLmsConfig(Long id) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with id: " + id));

        Map<String, Object> oldValue = convertToMap(lmsConfig);
        lmsConfig.setIsActive(true);
        lmsConfigRepository.save(lmsConfig);

        auditLogService.logUpdate("LmsConfiguration", lmsConfig.getId().toString(), lmsConfig.getLmsName(),
                oldValue, convertToMap(lmsConfig), List.of("isActive"));

        log.info("Activated LMS configuration: {} ({})", lmsConfig.getLmsName(), lmsConfig.getLmsCode());
    }

    @Override
    @Transactional
    public LmsConnectionTestResult testLmsConnection(Long id) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with id: " + id));

        long startTime = System.currentTimeMillis();
        TestStatus testStatus;
        String testMessage;
        String errorDetails = null;

        try {
            switch (lmsConfig.getLmsType()) {
                case CUSTOM_API -> {
                    // Test API endpoint
                    if (lmsConfig.getApiEndpoint() != null && !lmsConfig.getApiEndpoint().isEmpty()) {
                        URL url = new URL(lmsConfig.getApiEndpoint());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);
                        int responseCode = connection.getResponseCode();
                        if (responseCode >= 200 && responseCode < 300) {
                            testStatus = TestStatus.SUCCESS;
                            testMessage = "API connection successful (HTTP " + responseCode + ")";
                        } else {
                            testStatus = TestStatus.FAILED;
                            testMessage = "API returned HTTP " + responseCode;
                        }
                        connection.disconnect();
                    } else {
                        testStatus = TestStatus.FAILED;
                        testMessage = "No API endpoint configured";
                    }
                }
                case FINACLE, FLEXCUBE -> {
                    // Test database connection
                    if (lmsConfig.getConnectionUrl() != null && !lmsConfig.getConnectionUrl().isEmpty()) {
                        try (Connection conn = DriverManager.getConnection(
                                lmsConfig.getConnectionUrl(),
                                lmsConfig.getUsername(),
                                lmsConfig.getPassword())) {
                            testStatus = TestStatus.SUCCESS;
                            testMessage = "Database connection successful";
                        }
                    } else {
                        testStatus = TestStatus.FAILED;
                        testMessage = "No connection URL configured";
                    }
                }
                case FILE_BASED -> {
                    // Test file location accessibility
                    if (lmsConfig.getFileLocation() != null && !lmsConfig.getFileLocation().isEmpty()) {
                        java.io.File file = new java.io.File(lmsConfig.getFileLocation());
                        if (file.exists() && (file.isDirectory() || file.canRead())) {
                            testStatus = TestStatus.SUCCESS;
                            testMessage = "File location accessible";
                        } else {
                            testStatus = TestStatus.FAILED;
                            testMessage = "File location not accessible";
                        }
                    } else {
                        testStatus = TestStatus.FAILED;
                        testMessage = "No file location configured";
                    }
                }
                default -> {
                    testStatus = TestStatus.FAILED;
                    testMessage = "Unknown LMS type";
                }
            }
        } catch (Exception e) {
            testStatus = TestStatus.FAILED;
            testMessage = "Connection test failed";
            errorDetails = e.getMessage();
            log.error("LMS connection test failed for {}: {}", lmsConfig.getLmsCode(), e.getMessage());
        }

        int responseTimeMs = (int) (System.currentTimeMillis() - startTime);

        auditLogService.log("LMS_CONNECTION_TESTED", "LmsConfiguration", lmsConfig.getId().toString(),
                lmsConfig.getLmsName(), "TEST", null,
                Map.of("testStatus", testStatus.name(), "responseTimeMs", responseTimeMs), null);

        return LmsConnectionTestResult.builder()
                .lmsId(lmsConfig.getId())
                .lmsCode(lmsConfig.getLmsCode())
                .lmsName(lmsConfig.getLmsName())
                .testStatus(testStatus)
                .responseTimeMs(responseTimeMs)
                .testMessage(testMessage)
                .errorDetails(errorDetails)
                .testedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public LmsSyncHistoryDTO triggerManualSync(Long id) {
        LmsConfiguration lmsConfig = lmsConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LMS configuration not found with id: " + id));

        // Create sync history record
        LmsSyncHistory syncHistory = LmsSyncHistory.builder()
                .lmsConfiguration(lmsConfig)
                .syncType("MANUAL")
                .syncStatus(SyncStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .triggeredBy("MANUAL")
                .syncBatchId(UUID.randomUUID().toString())
                .build();

        syncHistory = syncHistoryRepository.save(syncHistory);

        // Note: Actual sync logic would be implemented here or delegated to a separate async service
        // For now, we just record the sync initiation

        auditLogService.log("LMS_SYNC_TRIGGERED", "LmsConfiguration", lmsConfig.getId().toString(),
                lmsConfig.getLmsName(), "SYNC", null,
                Map.of("syncType", "MANUAL", "syncBatchId", syncHistory.getSyncBatchId()), null);

        log.info("Manual sync triggered for LMS: {} ({}), batch: {}",
                lmsConfig.getLmsName(), lmsConfig.getLmsCode(), syncHistory.getSyncBatchId());

        return mapSyncHistoryToDTO(syncHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LmsConfigurationDTO> getAllLmsConfigs(LmsType type, Boolean isActive, String search, Pageable pageable) {
        return lmsConfigRepository.findWithFilters(type, isActive, search, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LmsSyncHistoryDTO> getSyncHistory(Long lmsId, Pageable pageable) {
        return syncHistoryRepository.findByLmsConfigurationId(lmsId, pageable)
                .map(this::mapSyncHistoryToDTO);
    }

    private LmsConfigurationDTO mapToDTO(LmsConfiguration lmsConfig) {
        return LmsConfigurationDTO.builder()
                .id(lmsConfig.getId())
                .lmsCode(lmsConfig.getLmsCode())
                .lmsName(lmsConfig.getLmsName())
                .lmsType(lmsConfig.getLmsType())
                .description(lmsConfig.getDescription())
                .connectionUrl(lmsConfig.getConnectionUrl())
                .databaseName(lmsConfig.getDatabaseName())
                .schemaName(lmsConfig.getSchemaName())
                .apiEndpoint(lmsConfig.getApiEndpoint())
                .apiAuthType(lmsConfig.getApiAuthType())
                .fileLocation(lmsConfig.getFileLocation())
                .fileFormat(lmsConfig.getFileFormat())
                .fileDelimiter(lmsConfig.getFileDelimiter())
                .fileEncoding(lmsConfig.getFileEncoding())
                .syncFrequency(lmsConfig.getSyncFrequency())
                .syncSchedule(lmsConfig.getSyncSchedule())
                .syncStartTime(lmsConfig.getSyncStartTime())
                .batchSize(lmsConfig.getBatchSize())
                .fieldMappings(lmsConfig.getFieldMappings())
                .lmsIdentifier(lmsConfig.getLmsIdentifier())
                .paymentTypeId(lmsConfig.getPaymentTypeId())
                .productTypes(lmsConfig.getProductTypes())
                .isActive(lmsConfig.getIsActive())
                .lastSyncAt(lmsConfig.getLastSyncAt())
                .lastSyncStatus(lmsConfig.getLastSyncStatus())
                .lastSyncMessage(lmsConfig.getLastSyncMessage())
                .lastSyncRecords(lmsConfig.getLastSyncRecords())
                .lastSyncDurationSeconds(lmsConfig.getLastSyncDurationSeconds())
                .totalRecordsSynced(lmsConfig.getTotalRecordsSynced())
                .createdAt(lmsConfig.getCreatedAt())
                .updatedAt(lmsConfig.getUpdatedAt())
                .createdBy(lmsConfig.getCreatedBy())
                .updatedBy(lmsConfig.getUpdatedBy())
                .build();
    }

    private LmsSyncHistoryDTO mapSyncHistoryToDTO(LmsSyncHistory history) {
        return LmsSyncHistoryDTO.builder()
                .id(history.getId())
                .lmsId(history.getLmsConfiguration().getId())
                .lmsCode(history.getLmsConfiguration().getLmsCode())
                .lmsName(history.getLmsConfiguration().getLmsName())
                .syncType(history.getSyncType())
                .syncStatus(history.getSyncStatus())
                .startedAt(history.getStartedAt())
                .completedAt(history.getCompletedAt())
                .durationSeconds(history.getDurationSeconds())
                .totalRecords(history.getTotalRecords())
                .newRecords(history.getNewRecords())
                .updatedRecords(history.getUpdatedRecords())
                .failedRecords(history.getFailedRecords())
                .skippedRecords(history.getSkippedRecords())
                .errorMessage(history.getErrorMessage())
                .errorDetails(history.getErrorDetails())
                .syncBatchId(history.getSyncBatchId())
                .triggeredBy(history.getTriggeredBy())
                .triggeredByUser(history.getTriggeredByUser())
                .createdAt(history.getCreatedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(LmsConfiguration lmsConfig) {
        try {
            return objectMapper.convertValue(lmsConfig, Map.class);
        } catch (Exception e) {
            log.warn("Failed to convert LMS config to map: {}", e.getMessage());
            return Map.of("id", lmsConfig.getId(), "lmsCode", lmsConfig.getLmsCode());
        }
    }
}

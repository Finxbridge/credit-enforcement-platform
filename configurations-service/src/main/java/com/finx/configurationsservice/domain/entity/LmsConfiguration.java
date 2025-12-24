package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.AuthType;
import com.finx.configurationsservice.domain.enums.LmsType;
import com.finx.configurationsservice.domain.enums.SyncFrequency;
import com.finx.configurationsservice.domain.enums.SyncStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "lms_configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "lms_code", unique = true, nullable = false, length = 50)
    private String lmsCode;

    @Column(name = "lms_name", nullable = false, length = 200)
    private String lmsName;

    @Enumerated(EnumType.STRING)
    @Column(name = "lms_type", nullable = false, length = 30)
    private LmsType lmsType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Connection Details
    @Column(name = "connection_url", length = 500)
    private String connectionUrl;

    @Column(name = "database_name", length = 100)
    private String databaseName;

    @Column(name = "schema_name", length = 100)
    private String schemaName;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password", length = 500)
    private String password;

    // API Configuration
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_auth_type", length = 30)
    private AuthType apiAuthType;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "api_secret", length = 500)
    private String apiSecret;

    // File Configuration
    @Column(name = "file_location", length = 500)
    private String fileLocation;

    @Column(name = "file_format", length = 20)
    private String fileFormat;

    @Column(name = "file_delimiter", length = 10)
    private String fileDelimiter;

    @Column(name = "file_encoding", length = 20)
    private String fileEncoding;

    // Sync Configuration
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_frequency", length = 30)
    private SyncFrequency syncFrequency;

    @Column(name = "sync_schedule", length = 100)
    private String syncSchedule;

    @Column(name = "sync_start_time")
    private LocalTime syncStartTime;

    @Column(name = "batch_size")
    private Integer batchSize;

    // Field Mappings
    @Type(JsonType.class)
    @Column(name = "field_mappings", columnDefinition = "jsonb")
    private Map<String, String> fieldMappings;

    // Identifiers
    @Column(name = "lms_identifier", length = 100)
    private String lmsIdentifier;

    @Column(name = "payment_type_id", length = 50)
    private String paymentTypeId;

    @Type(JsonType.class)
    @Column(name = "product_types", columnDefinition = "jsonb")
    private List<String> productTypes;

    // Status & Health
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_sync_status", length = 20)
    private SyncStatus lastSyncStatus;

    @Column(name = "last_sync_message", columnDefinition = "TEXT")
    private String lastSyncMessage;

    @Column(name = "last_sync_records")
    private Integer lastSyncRecords;

    @Column(name = "last_sync_duration_seconds")
    private Integer lastSyncDurationSeconds;

    @Column(name = "total_records_synced")
    private Long totalRecordsSynced;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (batchSize == null) {
            batchSize = 1000;
        }
        if (fileEncoding == null) {
            fileEncoding = "UTF-8";
        }
        if (syncFrequency == null) {
            syncFrequency = SyncFrequency.DAILY;
        }
        if (totalRecordsSynced == null) {
            totalRecordsSynced = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

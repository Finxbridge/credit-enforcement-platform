package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.SyncStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "lms_sync_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsSyncHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_id", nullable = false)
    private LmsConfiguration lmsConfiguration;

    @Column(name = "sync_type", nullable = false, length = 30)
    private String syncType; // FULL, INCREMENTAL, MANUAL

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "new_records")
    private Integer newRecords;

    @Column(name = "updated_records")
    private Integer updatedRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Column(name = "skipped_records")
    private Integer skippedRecords;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonType.class)
    @Column(name = "error_details", columnDefinition = "jsonb")
    private Map<String, Object> errorDetails;

    @Column(name = "sync_batch_id", length = 100)
    private String syncBatchId;

    @Column(name = "triggered_by", length = 30)
    private String triggeredBy; // SCHEDULER, MANUAL, WEBHOOK

    @Column(name = "triggered_by_user")
    private Long triggeredByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (totalRecords == null) totalRecords = 0;
        if (newRecords == null) newRecords = 0;
        if (updatedRecords == null) updatedRecords = 0;
        if (failedRecords == null) failedRecords = 0;
        if (skippedRecords == null) skippedRecords = 0;
    }
}

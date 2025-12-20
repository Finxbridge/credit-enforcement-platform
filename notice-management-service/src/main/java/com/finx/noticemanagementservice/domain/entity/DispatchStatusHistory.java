package com.finx.noticemanagementservice.domain.entity;

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
@Table(name = "dispatch_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private DispatchTracking dispatchTracking;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "source", length = 30)
    private String source; // SYSTEM, VENDOR_API, MANUAL

    @Type(JsonType.class)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private Map<String, Object> rawData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (source == null) {
            source = "SYSTEM";
        }
    }
}

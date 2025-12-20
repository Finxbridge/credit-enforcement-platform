package com.finx.noticemanagementservice.domain.entity;

import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispatch_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false, length = 100)
    private String trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "notice_number", length = 50)
    private String noticeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private NoticeVendor vendor;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(name = "service_type", length = 50)
    private String serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", length = 30)
    private DispatchStatus dispatchStatus;

    @Column(name = "current_location", length = 255)
    private String currentLocation;

    @Column(name = "current_status_remarks", columnDefinition = "TEXT")
    private String currentStatusRemarks;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "in_transit_at")
    private LocalDateTime inTransitAt;

    @Column(name = "out_for_delivery_at")
    private LocalDateTime outForDeliveryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "rto_initiated_at")
    private LocalDateTime rtoInitiatedAt;

    @Column(name = "rto_received_at")
    private LocalDateTime rtoReceivedAt;

    // Delivery Details
    @Column(name = "delivery_attempt_count")
    private Integer deliveryAttemptCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_attempt_status", length = 50)
    private String lastAttemptStatus;

    @Column(name = "last_attempt_remarks", columnDefinition = "TEXT")
    private String lastAttemptRemarks;

    // SLA Tracking
    @Column(name = "expected_dispatch_by")
    private LocalDateTime expectedDispatchBy;

    @Column(name = "expected_delivery_by")
    private LocalDateTime expectedDeliveryBy;

    @Column(name = "dispatch_sla_breached")
    private Boolean dispatchSlaBreached;

    @Column(name = "delivery_sla_breached")
    private Boolean deliverySlaBreached;

    @Column(name = "sla_breach_notified")
    private Boolean slaBreachNotified;

    // RTO Details
    @Column(name = "rto_reason", length = 255)
    private String rtoReason;

    @Column(name = "rto_action", length = 50)
    private String rtoAction;

    @Column(name = "rto_action_taken_at")
    private LocalDateTime rtoActionTakenAt;

    @Column(name = "rto_action_taken_by")
    private Long rtoActionTakenBy;

    // PoD
    @Column(name = "pod_id")
    private Long podId;

    @Column(name = "pod_uploaded_at")
    private LocalDateTime podUploadedAt;

    // Cost
    @Column(name = "dispatch_cost", precision = 10, scale = 2)
    private BigDecimal dispatchCost;

    // Audit
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dispatchStatus == null) {
            dispatchStatus = DispatchStatus.PENDING;
        }
        if (deliveryAttemptCount == null) {
            deliveryAttemptCount = 0;
        }
        if (dispatchSlaBreached == null) {
            dispatchSlaBreached = false;
        }
        if (deliverySlaBreached == null) {
            deliverySlaBreached = false;
        }
        if (slaBreachNotified == null) {
            slaBreachNotified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchTrackingDTO {
    private Long id;
    private String trackingId;
    private Long noticeId;
    private String noticeNumber;
    private Long vendorId;
    private String vendorName;
    private String trackingNumber;
    private String carrierName;
    private String serviceType;
    private DispatchStatus dispatchStatus;
    private String currentLocation;
    private String currentStatusRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime inTransitAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime rtoInitiatedAt;
    private LocalDateTime rtoReceivedAt;
    private Integer deliveryAttemptCount;
    private LocalDateTime lastAttemptAt;
    private String lastAttemptStatus;
    private String lastAttemptRemarks;
    private LocalDateTime expectedDispatchBy;
    private LocalDateTime expectedDeliveryBy;
    private Boolean dispatchSlaBreached;
    private Boolean deliverySlaBreached;
    private Boolean slaBreachNotified;
    private String rtoReason;
    private String rtoAction;
    private LocalDateTime rtoActionTakenAt;
    private Long rtoActionTakenBy;
    private Long podId;
    private LocalDateTime podUploadedAt;
    private BigDecimal dispatchCost;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}

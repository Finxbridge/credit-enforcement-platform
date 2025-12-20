package com.finx.noticemanagementservice.domain.dto;

import jakarta.validation.constraints.NotNull;
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
public class UploadPodRequest {

    @NotNull(message = "Notice ID is required")
    private Long noticeId;

    @NotNull(message = "POD type is required")
    private String podType;

    private String podFileUrl;

    private String podFileType;

    private Integer podFileSizeKb;

    private String recipientName;

    private String recipientRelationship;

    private String recipientSignatureUrl;

    private String recipientPhotoUrl;

    @NotNull(message = "Delivery time is required")
    private LocalDateTime deliveredAt;

    private String deliveredLocation;

    private BigDecimal gpsLatitude;

    private BigDecimal gpsLongitude;

    private String deliveryRemarks;

    private Long vendorId;

    private String vendorAgentName;

    private Long uploadedBy;
}

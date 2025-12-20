package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
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
public class ProofOfDeliveryDTO {
    private Long id;
    private String podNumber;
    private Long noticeId;
    private String noticeNumber;
    private String podType;
    private String podFileUrl;
    private String podFileType;
    private Integer podFileSizeKb;
    private String recipientName;
    private String recipientRelationship;
    private String recipientSignatureUrl;
    private String recipientPhotoUrl;
    private LocalDateTime deliveredAt;
    private String deliveredLocation;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private String deliveryRemarks;
    private Long vendorId;
    private String vendorName;
    private String vendorAgentName;
    private PodVerificationStatus verificationStatus;
    private Long verifiedBy;
    private LocalDateTime verifiedAt;
    private String verificationRemarks;
    private String rejectionReason;
    private LocalDateTime uploadedAt;
    private Long uploadedBy;
}

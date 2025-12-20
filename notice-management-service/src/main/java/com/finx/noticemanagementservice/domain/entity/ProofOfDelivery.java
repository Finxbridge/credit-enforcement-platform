package com.finx.noticemanagementservice.domain.entity;

import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice_proof_of_delivery")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pod_number", unique = true, nullable = false, length = 50)
    private String podNumber;

    @Column(name = "notice_id", nullable = false)
    private Long noticeId;

    @Column(name = "pod_type", nullable = false, length = 30)
    private String podType;

    @Column(name = "pod_file_url", length = 500)
    private String podFileUrl;

    @Column(name = "pod_file_type", length = 20)
    private String podFileType;

    @Column(name = "pod_file_size_kb")
    private Integer podFileSizeKb;

    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Column(name = "recipient_relationship", length = 50)
    private String recipientRelationship;

    @Column(name = "recipient_signature_url", length = 500)
    private String recipientSignatureUrl;

    @Column(name = "recipient_photo_url", length = 500)
    private String recipientPhotoUrl;

    @Column(name = "delivered_at", nullable = false)
    private LocalDateTime deliveredAt;

    @Column(name = "delivered_location", length = 255)
    private String deliveredLocation;

    @Column(name = "gps_latitude", precision = 10, scale = 8)
    private BigDecimal gpsLatitude;

    @Column(name = "gps_longitude", precision = 11, scale = 8)
    private BigDecimal gpsLongitude;

    @Column(name = "delivery_remarks", columnDefinition = "TEXT")
    private String deliveryRemarks;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "vendor_agent_name", length = 100)
    private String vendorAgentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    private PodVerificationStatus verificationStatus;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_remarks", columnDefinition = "TEXT")
    private String verificationRemarks;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (verificationStatus == null) {
            verificationStatus = PodVerificationStatus.PENDING;
        }
    }
}

package com.finx.noticemanagementservice.domain.entity;

import com.finx.noticemanagementservice.domain.enums.VendorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice_vendors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_code", unique = true, nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", nullable = false, length = 255)
    private String vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", nullable = false, length = 30)
    private VendorType vendorType;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_mobile", length = 15)
    private String contactMobile;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @Column(name = "service_areas", columnDefinition = "TEXT")
    private String serviceAreas;

    @Column(name = "default_dispatch_sla_hours")
    private Integer defaultDispatchSlaHours;

    @Column(name = "default_delivery_sla_days")
    private Integer defaultDeliverySlaDays;

    @Column(name = "cost_per_dispatch", precision = 10, scale = 2)
    private BigDecimal costPerDispatch;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "delivery_rate", precision = 5, scale = 2)
    private BigDecimal deliveryRate;

    @Column(name = "rto_rate", precision = 5, scale = 2)
    private BigDecimal rtoRate;

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
        if (priorityOrder == null) {
            priorityOrder = 0;
        }
        if (defaultDispatchSlaHours == null) {
            defaultDispatchSlaHours = 24;
        }
        if (defaultDeliverySlaDays == null) {
            defaultDeliverySlaDays = 7;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

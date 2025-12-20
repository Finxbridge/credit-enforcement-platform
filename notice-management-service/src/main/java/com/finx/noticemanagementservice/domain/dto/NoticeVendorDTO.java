package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.VendorType;
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
public class NoticeVendorDTO {
    private Long id;
    private String vendorCode;
    private String vendorName;
    private VendorType vendorType;
    private String contactPerson;
    private String contactEmail;
    private String contactMobile;
    private String address;
    private String apiEndpoint;
    private String serviceAreas;
    private Integer defaultDispatchSlaHours;
    private Integer defaultDeliverySlaDays;
    private BigDecimal costPerDispatch;
    private Boolean isActive;
    private Integer priorityOrder;
    private BigDecimal deliveryRate;
    private BigDecimal rtoRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

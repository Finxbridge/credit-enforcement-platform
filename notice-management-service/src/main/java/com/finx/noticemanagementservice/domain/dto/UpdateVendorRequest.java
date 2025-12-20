package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.VendorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVendorRequest {

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

    private Long updatedBy;
}

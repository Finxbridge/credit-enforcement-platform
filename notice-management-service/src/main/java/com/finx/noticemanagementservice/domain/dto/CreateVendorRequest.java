package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.VendorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVendorRequest {

    @NotBlank(message = "Vendor code is required")
    private String vendorCode;

    @NotBlank(message = "Vendor name is required")
    private String vendorName;

    @NotNull(message = "Vendor type is required")
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

    private Integer priorityOrder;

    private Long createdBy;
}

package com.finx.noticemanagementservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchNoticeRequest {

    @NotNull(message = "Notice ID is required")
    private Long noticeId;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    private String trackingNumber;

    private String carrierName;

    private LocalDateTime expectedDeliveryAt;

    private Long dispatchedBy;
}

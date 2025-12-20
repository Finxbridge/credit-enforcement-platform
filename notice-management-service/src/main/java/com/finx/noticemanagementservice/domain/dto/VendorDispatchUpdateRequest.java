package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDispatchUpdateRequest {

    @NotNull(message = "Status is required")
    private DispatchStatus status;

    private String trackingNumber;

    private String carrierName;

    private String serviceType;

    private String location;

    private String remarks;

    private LocalDateTime eventTimestamp;

    // For delivery/RTO
    private String recipientRelation; // Self, Relative, Security, etc.
    private String recipientSignature; // Base64 or URL

    // For failed attempts
    private String failureReason;
    private LocalDateTime nextAttemptDate;

    // Additional data from vendor
    private Map<String, Object> additionalData;
}

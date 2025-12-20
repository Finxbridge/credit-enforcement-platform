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
public class UpdateDispatchStatusRequest {

    @NotNull(message = "Status is required")
    private DispatchStatus status;

    private String location;
    private String remarks;
    private LocalDateTime eventTimestamp;
    private String source; // SYSTEM, VENDOR_API, MANUAL
    private Map<String, Object> rawData;
}

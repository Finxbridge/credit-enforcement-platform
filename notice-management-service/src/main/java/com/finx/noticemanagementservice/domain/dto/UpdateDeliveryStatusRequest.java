package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
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
public class UpdateDeliveryStatusRequest {

    @NotNull(message = "Notice ID is required")
    private Long noticeId;

    @NotNull(message = "Status is required")
    private NoticeStatus status;

    private LocalDateTime deliveredAt;

    private LocalDateTime rtoAt;

    private String rtoReason;

    private Long updatedBy;
}

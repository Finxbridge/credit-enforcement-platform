package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPodRequest {

    @NotNull(message = "POD ID is required")
    private Long podId;

    @NotNull(message = "Verification status is required")
    private PodVerificationStatus verificationStatus;

    private String verificationRemarks;

    private String rejectionReason;

    private Long verifiedBy;
}

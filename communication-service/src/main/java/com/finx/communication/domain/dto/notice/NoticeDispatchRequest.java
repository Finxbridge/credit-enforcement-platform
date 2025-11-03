package com.finx.communication.domain.dto.notice;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDispatchRequest {

    @NotNull(message = "Notice ID is required")
    private Long noticeId;

    @NotNull(message = "Case ID is required")
    private Long caseId;

    private String recipientName;
    private String recipientAddress;
    private String recipientMobile;
    private String recipientPincode;

    private String dispatchMethod; // COURIER, RPAD, HAND_DELIVERY

    private String vendorCode; // Specific vendor if required

    private String pdfUrl; // Notice PDF URL

    /**
     * Additional vendor-specific parameters
     */
    private Map<String, Object> additionalParams;
}

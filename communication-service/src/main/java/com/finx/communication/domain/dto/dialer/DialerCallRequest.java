package com.finx.communication.domain.dto.dialer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerCallRequest {

    @NotBlank(message = "Customer mobile is required")
    private String customerMobile;

    private String callType; // CLICK_TO_CALL, AUTO_DIAL, INBOUND

    private Long agentId;
    private Long caseId;

    private String agentNumber;
    private String disposition;
    private String notes;
}

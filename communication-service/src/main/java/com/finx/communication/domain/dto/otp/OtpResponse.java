package com.finx.communication.domain.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private String requestId;
    private String status; // SENT, VERIFIED, FAILED
    private String message;
    private String providerResponse;
}

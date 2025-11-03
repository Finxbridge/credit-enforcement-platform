package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerResponse {

    private String callId;
    private String dialerCallId;
    private String status;
    private String message;
    private String providerResponse;
}

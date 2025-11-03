package com.finx.communication.domain.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SMS Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {

    private String status;
    private String message;
    private List<String> messageIds;
    private String providerResponse;
}

package com.finx.communication.domain.dto.dialer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for updating call disposition after call completion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerDispositionRequest {

    @NotBlank(message = "Call ID is required")
    private String callId;

    @NotBlank(message = "Disposition code is required")
    private String dispositionCode;     // PTP, RTP, CALLBACK, NOT_INTERESTED, DISPUTE, etc.

    private String subDispositionCode;  // Sub-category of disposition

    private String notes;               // Agent notes/remarks

    private LocalDateTime callbackDateTime; // For PTP/callback dispositions

    private String promisedAmount;      // For PTP dispositions

    private Map<String, Object> customFields; // Additional disposition fields
}

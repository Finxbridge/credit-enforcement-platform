package com.finx.communication.domain.dto.dialer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk queue call requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerQueueRequest {

    @NotEmpty(message = "At least one customer must be provided")
    private List<CustomerQueueItem> customers;

    private String campaignId;       // Campaign/strategy ID for grouping
    private Long agentId;            // Target agent for calls
    private Integer priority;        // Queue priority (higher = more urgent)
    private String dialMode;         // PROGRESSIVE, PREDICTIVE, PREVIEW
    private Integer maxAttempts;     // Max call attempts per customer
    private Integer retryDelayMinutes; // Delay between retry attempts

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerQueueItem {
        @NotNull(message = "Case ID is required")
        private Long caseId;

        @NotNull(message = "Customer mobile is required")
        private String customerMobile;

        private String customerName;
        private String alternateNumbers; // Comma-separated alternate numbers
        private Integer priority;        // Individual customer priority
        private String scriptId;         // Call script to use
    }
}

package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for call history query parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerCallHistoryRequest {

    private Long caseId;
    private Long agentId;
    private String customerMobile;
    private String callStatus;         // ANSWERED, NO_ANSWER, BUSY, FAILED
    private String dispositionCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String campaignId;
    private Integer page;
    private Integer size;
    private String sortBy;             // createdAt, callDuration, disposition
    private String sortDirection;      // ASC, DESC
}

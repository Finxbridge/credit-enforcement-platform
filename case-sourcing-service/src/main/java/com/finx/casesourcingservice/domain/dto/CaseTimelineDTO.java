package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for case timeline (complete activity history)
 * FR-WF-2: Case activity timeline
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseTimelineDTO {

    private Long caseId;
    private String caseNumber;
    private String customerName;
    private String loanAccountNumber;

    private List<TimelineEventDTO> events;

    private TimelineSummaryDTO summary;
}

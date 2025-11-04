package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Intake Report Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeReportDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalReceived;
    private Long totalValidated;
    private Long totalFailed;
    private Double successRate;
    private List<IntakeReportItemDTO> dailyBreakdown;
    private List<SourceWiseIntakeDTO> sourceBreakdown;
}

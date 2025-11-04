package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for daily intake metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeReportItemDTO {

    private LocalDate date;
    private Long totalReceived;
    private Long validated;
    private Long failed;
    private Double successRate;
}

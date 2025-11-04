package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for daily unallocated case metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnallocatedReportItemDTO {

    private LocalDate date;
    private Long unallocatedCount;
    private String bucket;
    private String source;
}

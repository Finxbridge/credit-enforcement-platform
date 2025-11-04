package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Unallocated Cases Report Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnallocatedReportDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalUnallocated;
    private List<UnallocatedReportItemDTO> breakdown;
    private List<BucketWiseUnallocatedDTO> bucketBreakdown;
    private List<SourceWiseUnallocatedDTO> sourceBreakdown;
}

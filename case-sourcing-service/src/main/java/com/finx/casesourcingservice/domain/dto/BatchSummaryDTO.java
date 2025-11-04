package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSummaryDTO {
    private String batchId;
    private Integer validCases;
    private Integer invalidCases;
    private Integer duplicates;
}

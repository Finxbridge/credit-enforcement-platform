package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchStatusDTO {
    private String batchId;
    private Integer totalCases;
    private Integer validCases;
    private Integer invalidCases;
    private String status;
    private List<BatchErrorDTO> errors;
    private LocalDateTime completedAt;
}

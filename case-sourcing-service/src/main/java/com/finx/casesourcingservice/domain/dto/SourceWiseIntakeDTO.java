package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for source-wise intake breakdown
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceWiseIntakeDTO {

    private String source;
    private Long totalReceived;
    private Long validated;
    private Long failed;
    private Double successRate;
}

package com.finx.allocationreallocationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailsDTO {
    private BigDecimal totalOutstanding;
    private Integer dpd;
    private String bucket;
}

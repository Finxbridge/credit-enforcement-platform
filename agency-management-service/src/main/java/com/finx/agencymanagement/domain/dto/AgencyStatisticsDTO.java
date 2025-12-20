package com.finx.agencymanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for overall agency statistics (admin view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyStatisticsDTO {

    private Long totalAgencies;
    private Long activeAgencies;
    private Long pendingApprovalAgencies;
    private Long suspendedAgencies;
    private Long inactiveAgencies;
    private Long rejectedAgencies;

    private Long totalAgencyUsers;
    private Long activeAgencyUsers;

    private Long totalAllocatedCases;
    private Long activeAllocations;

    private BigDecimal totalOutstandingAmount;
    private BigDecimal totalCollectedAmount;
}

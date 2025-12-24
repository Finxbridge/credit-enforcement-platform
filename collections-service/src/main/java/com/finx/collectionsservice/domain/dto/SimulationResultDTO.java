package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Rule Simulation Result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResultDTO {

    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String closureReason;
    private Integer totalEligibleCases;
    private BigDecimal totalOutstandingAmount;
    private List<EligibleCaseDTO> eligibleCases;
    private String simulatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligibleCaseDTO {
        private Long caseId;
        private String caseNumber;
        private String loanAccountNumber;
        private String customerName;
        private BigDecimal outstandingAmount;
        private Integer dpd;
        private String bucket;
        private String currentStatus;
        private String lastActivityDate;
        private Integer daysSinceLastActivity;
        private Integer daysWithZeroOutstanding;
    }
}

package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simplified Case DTO for Strategy Simulation Response
 * Contains only essential fields needed for simulation preview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationCaseDTO {

    // Case Information
    private String caseNumber;
    private String externalCaseId;
    private String caseStatus;

    // Customer Information
    private String customerName;
    private String mobileNumber;
    private String city;
    private String state;

    // Loan Information
    private String loanAccountNumber;
    private String productType;
    private BigDecimal loanAmount;
    private BigDecimal totalOutstanding;
    private BigDecimal emiAmount;

    // DPD and Overdue
    private Integer dpd;
    private Integer paidEmi;
    private Integer pendingEmi;
    private LocalDate dueDate;
    private LocalDate lastPaymentDate;
}

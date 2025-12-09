package com.finx.casesourcingservice.domain.dto.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified DTO representing a single row in the case upload CSV.
 * This DTO is used for:
 * - Case Sourcing (PRIMARY_AGENT, SECONDARY_AGENT optional)
 * - Allocation (PRIMARY_AGENT mandatory, SECONDARY_AGENT optional)
 * - Reallocation (PRIMARY_AGENT mandatory, SECONDARY_AGENT optional)
 * - Export/Sample Download
 *
 * Mandatory Fields: CUSTOMER NAME, MOBILE NO, OVERDUE AMOUNT, EMI START DATE,
 *                   PRIMARY ADDRESS, CITY, STATE, PINCODE, PRODUCT, LOCATION, DPD, LANGUAGE
 *
 * For export failed/batches: STATUS and REMARKS columns are included
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCsvRowDTO {

    // Internal tracking (not in CSV)
    private Integer rowNumber;

    // ==================== LENDER & ACCOUNT ====================

    @CsvBindByName(column = "LENDER")
    private String lender;

    @CsvBindByName(column = "ACCOUNT NO")
    private String accountNo;

    // ==================== CUSTOMER INFORMATION (MANDATORY) ====================

    @CsvBindByName(column = "CUSTOMER NAME", required = true)
    private String customerName;

    @CsvBindByName(column = "MOBILE NO", required = true)
    private String mobileNo;

    @CsvBindByName(column = "CUSTOMER ID")
    private String customerId;

    @CsvBindByName(column = "EMAIL")
    private String email;

    @CsvBindByName(column = "SECONDARY MOBILE NUMBER")
    private String secondaryMobileNumber;

    @CsvBindByName(column = "RESI PHONE")
    private String resiPhone;

    @CsvBindByName(column = "ADDITIONAL PHONE 2")
    private String additionalPhone2;

    // ==================== ADDRESS (MANDATORY) ====================

    @CsvBindByName(column = "PRIMARY ADDRESS", required = true)
    private String primaryAddress;

    @CsvBindByName(column = "SECONDARY ADDRESS")
    private String secondaryAddress;

    @CsvBindByName(column = "CITY", required = true)
    private String city;

    @CsvBindByName(column = "STATE", required = true)
    private String state;

    @CsvBindByName(column = "PINCODE", required = true)
    private String pincode;

    // ==================== LOAN FINANCIAL DETAILS ====================

    @CsvBindByName(column = "OVERDUE AMOUNT", required = true)
    private String overdueAmount;

    @CsvBindByName(column = "POS")
    private String pos;

    @CsvBindByName(column = "TOS")
    private String tos;

    @CsvBindByName(column = "LOAN AMOUNT OR LIMIT")
    private String loanAmountOrLimit;

    @CsvBindByName(column = "EMI AMOUNT")
    private String emiAmount;

    @CsvBindByName(column = "PENALTY AMOUNT")
    private String penaltyAmount;

    @CsvBindByName(column = "CHARGES")
    private String charges;

    @CsvBindByName(column = "OD INTEREST")
    private String odInterest;

    // ==================== OVERDUE BREAKDOWN ====================

    @CsvBindByName(column = "PRINCIPAL OVERDUE")
    private String principalOverdue;

    @CsvBindByName(column = "INTEREST OVERDUE")
    private String interestOverdue;

    @CsvBindByName(column = "FEES OVERDUE")
    private String feesOverdue;

    @CsvBindByName(column = "PENALTY OVERDUE")
    private String penaltyOverdue;

    // ==================== EMI DETAILS (MANDATORY: EMI START DATE) ====================

    @CsvBindByName(column = "EMI START DATE", required = true)
    private String emiStartDate;

    @CsvBindByName(column = "NO OF PAID EMI")
    private String noOfPaidEmi;

    @CsvBindByName(column = "NO OF PENDING EMI")
    private String noOfPendingEmi;

    @CsvBindByName(column = "Emi Overdue From")
    private String emiOverdueFrom;

    @CsvBindByName(column = "Next EMI Date")
    private String nextEmiDate;

    // ==================== LOAN TENURE ====================

    @CsvBindByName(column = "LOAN DURATION")
    private String loanDuration;

    @CsvBindByName(column = "ROI")
    private String roi;

    // ==================== IMPORTANT DATES ====================

    @CsvBindByName(column = "DATE OF DISBURSEMENT")
    private String dateOfDisbursement;

    @CsvBindByName(column = "MATURITY DATE")
    private String maturityDate;

    @CsvBindByName(column = "DUE DATE")
    private String dueDate;

    @CsvBindByName(column = "WRITEOFF DATE")
    private String writeoffDate;

    // ==================== DPD & BUCKET (MANDATORY: DPD) ====================

    @CsvBindByName(column = "DPD", required = true)
    private String dpd;

    @CsvBindByName(column = "RISK BUCKET")
    private String riskBucket;

    @CsvBindByName(column = "SOM BUCKET")
    private String somBucket;

    @CsvBindByName(column = "SOM DPD")
    private String somDpd;

    @CsvBindByName(column = "CYCLE DUE")
    private String cycleDue;

    // ==================== PRODUCT & SCHEME (MANDATORY: PRODUCT) ====================

    @CsvBindByName(column = "PRODUCT", required = true)
    private String product;

    @CsvBindByName(column = "SCHEME CODE")
    private String schemeCode;

    @CsvBindByName(column = "PRODUCT SOURCING TYPE")
    private String productSourcingType;

    // ==================== CREDIT CARD SPECIFIC ====================

    @CsvBindByName(column = "MINIMUM AMOUNT DUE")
    private String minimumAmountDue;

    @CsvBindByName(column = "CARD OUTSTANDING")
    private String cardOutstanding;

    @CsvBindByName(column = "STATEMENT DATE")
    private String statementDate;

    @CsvBindByName(column = "STATEMENT MONTH")
    private String statementMonth;

    @CsvBindByName(column = "CARD STATUS")
    private String cardStatus;

    @CsvBindByName(column = "LAST BILLED AMOUNT")
    private String lastBilledAmount;

    @CsvBindByName(column = "LAST 4 DIGITS")
    private String last4Digits;

    // ==================== PAYMENT INFORMATION ====================

    @CsvBindByName(column = "LAST PAYMENT DATE")
    private String lastPaymentDate;

    @CsvBindByName(column = "LAST PAYMENT MODE")
    private String lastPaymentMode;

    @CsvBindByName(column = "LAST PAID AMOUNT")
    private String lastPaidAmount;

    // ==================== REPAYMENT BANK DETAILS ====================

    @CsvBindByName(column = "BENEFICIARY ACCOUNT Number")
    private String beneficiaryAccountNumber;

    @CsvBindByName(column = "BENEFICIARY ACCOUNT NAME")
    private String beneficiaryAccountName;

    @CsvBindByName(column = "REPAYMENT BANK NAME")
    private String repaymentBankName;

    @CsvBindByName(column = "REPAYMENT IFSC CODE")
    private String repaymentIfscCode;

    @CsvBindByName(column = "REFERENCE URL")
    private String referenceUrl;

    // ==================== LENDER REFERENCES ====================

    @CsvBindByName(column = "REFERENCE LENDER")
    private String referenceLender;

    @CsvBindByName(column = "CO LENDER")
    private String coLender;

    // ==================== FAMILY & EMPLOYMENT ====================

    @CsvBindByName(column = "FATHER SPOUSE NAME")
    private String fatherSpouseName;

    @CsvBindByName(column = "EMPLOYER OR BUSINESS ENTITY")
    private String employerOrBusinessEntity;

    // ==================== REFERENCES ====================

    @CsvBindByName(column = "REFERENCE 1 NAME")
    private String reference1Name;

    @CsvBindByName(column = "REFERENCE 1 NUMBER")
    private String reference1Number;

    @CsvBindByName(column = "REFERENCE 2 NAME")
    private String reference2Name;

    @CsvBindByName(column = "REFERENCE 2 NUMBER")
    private String reference2Number;

    // ==================== BLOCK STATUS ====================

    @CsvBindByName(column = "BLOCK 1")
    private String block1;

    @CsvBindByName(column = "BLOCK 1 DATE")
    private String block1Date;

    @CsvBindByName(column = "BLOCK 2")
    private String block2;

    @CsvBindByName(column = "BLOCK 2 DATE")
    private String block2Date;

    // ==================== LOCATION & GEOGRAPHY (MANDATORY: LOCATION, LANGUAGE) ====================

    @CsvBindByName(column = "LOCATION", required = true)
    private String location;

    @CsvBindByName(column = "ZONE")
    private String zone;

    @CsvBindByName(column = "LANGUAGE", required = true)
    private String language;

    // ==================== AGENT ALLOCATION ====================
    // For Case Sourcing: Optional
    // For Allocation/Reallocation: PRIMARY_AGENT Mandatory, SECONDARY_AGENT Optional

    @CsvBindByName(column = "PRIMARY AGENT")
    private String primaryAgent;

    @CsvBindByName(column = "SECONDARY AGENT")
    private String secondaryAgent;

    // ==================== SOURCING ====================

    @CsvBindByName(column = "SOURCING RM NAME")
    private String sourcingRmName;

    // ==================== FLAGS ====================

    @CsvBindByName(column = "REVIEW FLAG")
    private String reviewFlag;

    // ==================== ASSET DETAILS ====================

    @CsvBindByName(column = "ASSET DETAILS")
    private String assetDetails;

    @CsvBindByName(column = "VEHICLE REGISTRATION NUMBER")
    private String vehicleRegistrationNumber;

    @CsvBindByName(column = "VEHICLE IDENTIFICATION NUMBER")
    private String vehicleIdentificationNumber;

    @CsvBindByName(column = "CHASSIS NUMBER")
    private String chassisNumber;

    @CsvBindByName(column = "MODEL MAKE")
    private String modelMake;

    @CsvBindByName(column = "BATTERY ID")
    private String batteryId;

    // ==================== DEALER ====================

    @CsvBindByName(column = "DEALER NAME")
    private String dealerName;

    @CsvBindByName(column = "DEALER ADDRESS")
    private String dealerAddress;

    // ==================== AGENCY ====================

    @CsvBindByName(column = "AGENCY NAME")
    private String agencyName;

    // ==================== EXPORT/BATCH SPECIFIC (For failed exports) ====================

    @CsvBindByName(column = "STATUS")
    private String status;

    @CsvBindByName(column = "REMARKS")
    private String remarks;
}

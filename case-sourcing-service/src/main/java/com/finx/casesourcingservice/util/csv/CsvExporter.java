package com.finx.casesourcingservice.util.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.casesourcingservice.domain.entity.BatchError;
import com.finx.casesourcingservice.domain.entity.Case;
import com.finx.casesourcingservice.domain.entity.Customer;
import com.finx.casesourcingservice.domain.entity.LoanDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting data to CSV format
 * All exports use the same 88 columns as CaseCsvRowDTO for consistency
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvExporter {

    private final ObjectMapper objectMapper;

    private static final String CSV_DELIMITER = ",";
    private static final String LINE_SEPARATOR = "\n";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * All 88 column headers matching CaseCsvRowDTO.java exactly
     * Same order as template download and upload parsing
     */
    private static final String CSV_HEADER = String.join(CSV_DELIMITER,
            // Lender & Account
            "LENDER", "ACCOUNT NO",
            // Customer Information
            "CUSTOMER NAME", "MOBILE NO", "CUSTOMER ID", "EMAIL",
            "SECONDARY MOBILE NUMBER", "RESI PHONE", "ADDITIONAL PHONE 2",
            // Address
            "PRIMARY ADDRESS", "SECONDARY ADDRESS", "CITY", "STATE", "PINCODE",
            // Loan Financial Details
            "OVERDUE AMOUNT", "POS", "TOS", "LOAN AMOUNT OR LIMIT", "EMI AMOUNT",
            "PENALTY AMOUNT", "CHARGES", "OD INTEREST",
            // Overdue Breakdown
            "PRINCIPAL OVERDUE", "INTEREST OVERDUE", "FEES OVERDUE", "PENALTY OVERDUE",
            // EMI Details
            "EMI START DATE", "NO OF PAID EMI", "NO OF PENDING EMI", "Emi Overdue From", "Next EMI Date",
            // Loan Tenure
            "LOAN DURATION", "ROI",
            // Important Dates
            "DATE OF DISBURSEMENT", "MATURITY DATE", "DUE DATE", "WRITEOFF DATE",
            // DPD & Bucket
            "DPD", "RISK BUCKET", "SOM BUCKET", "SOM DPD", "CYCLE DUE",
            // Product & Scheme
            "PRODUCT", "SCHEME CODE", "PRODUCT SOURCING TYPE",
            // Credit Card Specific
            "MINIMUM AMOUNT DUE", "CARD OUTSTANDING", "STATEMENT DATE", "STATEMENT MONTH",
            "CARD STATUS", "LAST BILLED AMOUNT", "LAST 4 DIGITS",
            // Payment Information
            "LAST PAYMENT DATE", "LAST PAYMENT MODE", "LAST PAID AMOUNT",
            // Repayment Bank Details
            "BENEFICIARY ACCOUNT Number", "BENEFICIARY ACCOUNT NAME",
            "REPAYMENT BANK NAME", "REPAYMENT IFSC CODE", "REFERENCE URL",
            // Lender References
            "REFERENCE LENDER", "CO LENDER",
            // Family & Employment
            "FATHER SPOUSE NAME", "EMPLOYER OR BUSINESS ENTITY",
            // References
            "REFERENCE 1 NAME", "REFERENCE 1 NUMBER", "REFERENCE 2 NAME", "REFERENCE 2 NUMBER",
            // Block Status
            "BLOCK 1", "BLOCK 1 DATE", "BLOCK 2", "BLOCK 2 DATE",
            // Location & Geography
            "LOCATION", "ZONE", "LANGUAGE",
            // Agent Allocation
            "PRIMARY AGENT", "SECONDARY AGENT",
            // Sourcing
            "SOURCING RM NAME",
            // Flags
            "REVIEW FLAG",
            // Asset Details
            "ASSET DETAILS", "VEHICLE REGISTRATION NUMBER", "VEHICLE IDENTIFICATION NUMBER",
            "CHASSIS NUMBER", "MODEL MAKE", "BATTERY ID",
            // Dealer
            "DEALER NAME", "DEALER ADDRESS",
            // Agency
            "AGENCY NAME",
            // Export-specific (for error/status tracking)
            "STATUS", "REMARKS"
    );

    /**
     * Export failed cases (batch errors) to CSV with full 88 columns
     * Includes STATUS and REMARKS columns for error details
     */
    public byte[] exportBatchErrors(List<BatchError> errors) {
        log.info("Exporting {} batch errors to CSV", errors.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header (same 88 columns)
        writer.write(CSV_HEADER);
        writer.write(LINE_SEPARATOR);

        // Write data rows - mostly empty with STATUS and REMARKS filled
        for (BatchError error : errors) {
            // Write 86 empty columns (we don't have the original data)
            for (int i = 0; i < 86; i++) {
                if (i == 1) { // ACCOUNT NO column
                    writer.write(escapeCsv(error.getExternalCaseId()));
                }
                writer.write(CSV_DELIMITER);
            }
            // STATUS column - always FAILURE for error export
            writer.write("FAILURE");
            writer.write(CSV_DELIMITER);
            // REMARKS column - error details
            writer.write(escapeCsv("Row " + error.getRowNumber() + ": " + error.getErrorMessage()));
            writer.write(LINE_SEPARATOR);
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Export cases to CSV with full 88 columns
     * Same format as template download - can be re-uploaded after editing
     * STATUS shows SUCCESS for all exported cases
     */
    public byte[] exportCases(List<Case> cases) {
        log.info("Exporting {} cases to CSV", cases.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header (same 88 columns)
        writer.write(CSV_HEADER);
        writer.write(LINE_SEPARATOR);

        // Write data rows - all cases are SUCCESS
        for (Case caseEntity : cases) {
            writeCaseRow(writer, caseEntity, "SUCCESS", "");
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Export cases with status and remarks (for failed exports or status reports)
     */
    public byte[] exportCasesWithStatus(List<Case> cases, List<String> statuses, List<String> remarks) {
        log.info("Exporting {} cases with status to CSV", cases.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header (same 88 columns)
        writer.write(CSV_HEADER);
        writer.write(LINE_SEPARATOR);

        // Write data rows
        for (int i = 0; i < cases.size(); i++) {
            String status = (statuses != null && i < statuses.size()) ? statuses.get(i) : "";
            String remark = (remarks != null && i < remarks.size()) ? remarks.get(i) : "";
            writeCaseRow(writer, cases.get(i), status, remark);
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Write a single case row with all 88 columns
     */
    private void writeCaseRow(PrintWriter writer, Case caseEntity, String status, String remark) {
        LoanDetails loan = caseEntity.getLoan();
        Customer customer = loan != null ? loan.getPrimaryCustomer() : null;

        // Lender & Account
        writer.write(escapeCsv(loan != null ? loan.getLender() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getLoanAccountNumber() : ""));
        writer.write(CSV_DELIMITER);

        // Customer Information
        writer.write(escapeCsv(customer != null ? customer.getFullName() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getMobileNumber() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getCustomerId() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getEmail() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getSecondaryMobileNumber() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getResiPhone() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getAdditionalPhone2() : ""));
        writer.write(CSV_DELIMITER);

        // Address
        writer.write(escapeCsv(customer != null ? customer.getPrimaryAddress() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getSecondaryAddress() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getCity() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getState() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getPincode() : ""));
        writer.write(CSV_DELIMITER);

        // Loan Financial Details
        writer.write(formatDecimal(loan != null ? loan.getTotalOutstanding() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getPos() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getTos() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getLoanAmount() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getEmiAmount() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getPenaltyAmount() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getCharges() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getOdInterest() : null));
        writer.write(CSV_DELIMITER);

        // Overdue Breakdown
        writer.write(formatDecimal(loan != null ? loan.getPrincipalOverdue() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getInterestOverdue() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getFeesOverdue() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getPenaltyOverdue() : null));
        writer.write(CSV_DELIMITER);

        // EMI Details
        writer.write(formatDate(loan != null ? loan.getEmiStartDate() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatInteger(loan != null ? loan.getNoOfPaidEmi() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatInteger(loan != null ? loan.getNoOfPendingEmi() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getEmiOverdueFrom() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getNextEmiDate() : null));
        writer.write(CSV_DELIMITER);

        // Loan Tenure
        writer.write(escapeCsv(loan != null ? loan.getLoanDuration() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getRoi() : null));
        writer.write(CSV_DELIMITER);

        // Important Dates
        writer.write(formatDate(loan != null ? loan.getLoanDisbursementDate() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getLoanMaturityDate() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getDueDate() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getWriteoffDate() : null));
        writer.write(CSV_DELIMITER);

        // DPD & Bucket
        writer.write(formatInteger(loan != null ? loan.getDpd() : null));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getRiskBucket() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getSomBucket() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(formatInteger(loan != null ? loan.getSomDpd() : null));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getCycleDue() : ""));
        writer.write(CSV_DELIMITER);

        // Product & Scheme
        writer.write(escapeCsv(loan != null ? loan.getProductType() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getSchemeCode() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getProductSourcingType() : ""));
        writer.write(CSV_DELIMITER);

        // Credit Card Specific
        writer.write(formatDecimal(loan != null ? loan.getMinimumAmountDue() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getCardOutstanding() : null));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getStatementDate() : null));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getStatementMonth() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getCardStatus() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getLastBilledAmount() : null));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getLast4Digits() : ""));
        writer.write(CSV_DELIMITER);

        // Payment Information
        writer.write(formatDate(loan != null ? loan.getLastPaymentDate() : null));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getLastPaymentMode() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(formatDecimal(loan != null ? loan.getLastPaidAmount() : null));
        writer.write(CSV_DELIMITER);

        // Repayment Bank Details
        writer.write(escapeCsv(loan != null ? loan.getBeneficiaryAccountNumber() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getBeneficiaryAccountName() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getRepaymentBankName() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getRepaymentIfscCode() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getReferenceUrl() : ""));
        writer.write(CSV_DELIMITER);

        // Lender References
        writer.write(escapeCsv(loan != null ? loan.getReferenceLender() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getCoLender() : ""));
        writer.write(CSV_DELIMITER);

        // Family & Employment
        writer.write(escapeCsv(customer != null ? customer.getFatherSpouseName() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getEmployerOrBusinessEntity() : ""));
        writer.write(CSV_DELIMITER);

        // References
        writer.write(escapeCsv(customer != null ? customer.getReference1Name() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getReference1Number() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getReference2Name() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getReference2Number() : ""));
        writer.write(CSV_DELIMITER);

        // Block Status
        writer.write(escapeCsv(loan != null ? loan.getBlock1() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getBlock1Date() : null));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(loan != null ? loan.getBlock2() : ""));
        writer.write(CSV_DELIMITER);
        writer.write(formatDate(loan != null ? loan.getBlock2Date() : null));
        writer.write(CSV_DELIMITER);

        // Location & Geography
        writer.write(escapeCsv(caseEntity.getLocation()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getZone()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(customer != null ? customer.getLanguagePreference() : ""));
        writer.write(CSV_DELIMITER);

        // Agent Allocation
        writer.write(escapeCsv(caseEntity.getPrimaryAgent()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getSecondaryAgent()));
        writer.write(CSV_DELIMITER);

        // Sourcing
        writer.write(escapeCsv(loan != null ? loan.getSourcingRmName() : ""));
        writer.write(CSV_DELIMITER);

        // Flags
        writer.write(escapeCsv(caseEntity.getReviewFlag()));
        writer.write(CSV_DELIMITER);

        // Asset Details
        writer.write(escapeCsv(caseEntity.getAssetDetails()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getVehicleRegistrationNumber()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getVehicleIdentificationNumber()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getChassisNumber()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getModelMake()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getBatteryId()));
        writer.write(CSV_DELIMITER);

        // Dealer
        writer.write(escapeCsv(caseEntity.getDealerName()));
        writer.write(CSV_DELIMITER);
        writer.write(escapeCsv(caseEntity.getDealerAddress()));
        writer.write(CSV_DELIMITER);

        // Agency
        writer.write(escapeCsv(caseEntity.getAgencyName()));
        writer.write(CSV_DELIMITER);

        // STATUS
        writer.write(escapeCsv(status != null ? status : caseEntity.getCaseStatus()));
        writer.write(CSV_DELIMITER);

        // REMARKS
        writer.write(escapeCsv(remark != null ? remark : ""));
        writer.write(LINE_SEPARATOR);
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quotes, or newline, wrap it in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Escape existing quotes by doubling them
            value = value.replace("\"", "\"\"");
            // Wrap in quotes
            return "\"" + value + "\"";
        }

        return value;
    }

    private String formatDecimal(java.math.BigDecimal value) {
        return value != null ? value.toPlainString() : "";
    }

    private String formatInteger(Integer value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    /**
     * Export ALL batch data - both successful cases and errors combined
     * Used for "Export Cases" which should include both success and failure records
     * SUCCESS cases are exported from Case entity with STATUS="SUCCESS"
     * FAILURE cases are exported from BatchError entity with STATUS="FAILURE"
     */
    public byte[] exportAllBatchData(List<Case> successCases, List<BatchError> errors) {
        log.info("Exporting batch data: {} success cases, {} errors",
                successCases != null ? successCases.size() : 0,
                errors != null ? errors.size() : 0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header (same 88 columns)
        writer.write(CSV_HEADER);
        writer.write(LINE_SEPARATOR);

        // Write successful cases with STATUS="SUCCESS"
        if (successCases != null) {
            for (Case caseEntity : successCases) {
                writeCaseRow(writer, caseEntity, "SUCCESS", "");
            }
        }

        // Write error records with STATUS="FAILURE"
        if (errors != null) {
            for (BatchError error : errors) {
                writeErrorRow(writer, error);
            }
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Write a single error row with all 88 columns
     * Uses original row data (JSON) if available, otherwise falls back to just ACCOUNT NO
     */
    private void writeErrorRow(PrintWriter writer, BatchError error) {
        String originalRowData = error.getOriginalRowData();

        if (originalRowData != null && !originalRowData.isEmpty()) {
            // Parse JSON and write all original fields
            writeErrorRowFromJson(writer, originalRowData, error);
        } else {
            // Fallback: write minimal data
            writeErrorRowMinimal(writer, error);
        }
    }

    /**
     * Write error row using original JSON data - preserves all uploaded data
     */
    private void writeErrorRowFromJson(PrintWriter writer, String json, BatchError error) {
        try {
            JsonNode node = objectMapper.readTree(json);

            // Write all 86 data columns from JSON (matching CaseCsvRowDTO field names)
            // Lender & Account
            writer.write(escapeCsv(getJsonString(node, "lender")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "accountNo")));
            writer.write(CSV_DELIMITER);
            // Customer Information
            writer.write(escapeCsv(getJsonString(node, "customerName")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "mobileNo")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "customerId")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "email")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "secondaryMobileNumber")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "resiPhone")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "additionalPhone2")));
            writer.write(CSV_DELIMITER);
            // Address
            writer.write(escapeCsv(getJsonString(node, "primaryAddress")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "secondaryAddress")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "city")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "state")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "pincode")));
            writer.write(CSV_DELIMITER);
            // Loan Financial Details
            writer.write(escapeCsv(getJsonString(node, "overdueAmount")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "pos")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "tos")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "loanAmountOrLimit")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "emiAmount")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "penaltyAmount")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "charges")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "odInterest")));
            writer.write(CSV_DELIMITER);
            // Overdue Breakdown
            writer.write(escapeCsv(getJsonString(node, "principalOverdue")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "interestOverdue")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "feesOverdue")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "penaltyOverdue")));
            writer.write(CSV_DELIMITER);
            // EMI Details
            writer.write(escapeCsv(getJsonString(node, "emiStartDate")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "noOfPaidEmi")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "noOfPendingEmi")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "emiOverdueFrom")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "nextEmiDate")));
            writer.write(CSV_DELIMITER);
            // Loan Tenure
            writer.write(escapeCsv(getJsonString(node, "loanDuration")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "roi")));
            writer.write(CSV_DELIMITER);
            // Important Dates
            writer.write(escapeCsv(getJsonString(node, "dateOfDisbursement")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "maturityDate")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "dueDate")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "writeoffDate")));
            writer.write(CSV_DELIMITER);
            // DPD & Bucket
            writer.write(escapeCsv(getJsonString(node, "dpd")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "riskBucket")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "somBucket")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "somDpd")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "cycleDue")));
            writer.write(CSV_DELIMITER);
            // Product & Scheme
            writer.write(escapeCsv(getJsonString(node, "product")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "schemeCode")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "productSourcingType")));
            writer.write(CSV_DELIMITER);
            // Credit Card Specific
            writer.write(escapeCsv(getJsonString(node, "minimumAmountDue")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "cardOutstanding")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "statementDate")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "statementMonth")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "cardStatus")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "lastBilledAmount")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "last4Digits")));
            writer.write(CSV_DELIMITER);
            // Payment Information
            writer.write(escapeCsv(getJsonString(node, "lastPaymentDate")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "lastPaymentMode")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "lastPaidAmount")));
            writer.write(CSV_DELIMITER);
            // Repayment Bank Details
            writer.write(escapeCsv(getJsonString(node, "beneficiaryAccountNumber")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "beneficiaryAccountName")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "repaymentBankName")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "repaymentIfscCode")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "referenceUrl")));
            writer.write(CSV_DELIMITER);
            // Lender References
            writer.write(escapeCsv(getJsonString(node, "referenceLender")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "coLender")));
            writer.write(CSV_DELIMITER);
            // Family & Employment
            writer.write(escapeCsv(getJsonString(node, "fatherSpouseName")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "employerOrBusinessEntity")));
            writer.write(CSV_DELIMITER);
            // References
            writer.write(escapeCsv(getJsonString(node, "reference1Name")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "reference1Number")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "reference2Name")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "reference2Number")));
            writer.write(CSV_DELIMITER);
            // Block Status
            writer.write(escapeCsv(getJsonString(node, "block1")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "block1Date")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "block2")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "block2Date")));
            writer.write(CSV_DELIMITER);
            // Location & Geography
            writer.write(escapeCsv(getJsonString(node, "location")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "zone")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "language")));
            writer.write(CSV_DELIMITER);
            // Agent Allocation
            writer.write(escapeCsv(getJsonString(node, "primaryAgent")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "secondaryAgent")));
            writer.write(CSV_DELIMITER);
            // Sourcing
            writer.write(escapeCsv(getJsonString(node, "sourcingRmName")));
            writer.write(CSV_DELIMITER);
            // Flags
            writer.write(escapeCsv(getJsonString(node, "reviewFlag")));
            writer.write(CSV_DELIMITER);
            // Asset Details
            writer.write(escapeCsv(getJsonString(node, "assetDetails")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "vehicleRegistrationNumber")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "vehicleIdentificationNumber")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "chassisNumber")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "modelMake")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "batteryId")));
            writer.write(CSV_DELIMITER);
            // Dealer
            writer.write(escapeCsv(getJsonString(node, "dealerName")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "dealerAddress")));
            writer.write(CSV_DELIMITER);
            // Agency
            writer.write(escapeCsv(getJsonString(node, "agencyName")));
            writer.write(CSV_DELIMITER);
            // STATUS - FAILURE for error records
            writer.write("FAILURE");
            writer.write(CSV_DELIMITER);
            // REMARKS - error details with row number
            writer.write(escapeCsv("Row " + error.getRowNumber() + ": " + error.getErrorMessage()));
            writer.write(LINE_SEPARATOR);
        } catch (Exception e) {
            log.warn("Failed to parse original row data JSON, using minimal export: {}", e.getMessage());
            writeErrorRowMinimal(writer, error);
        }
    }

    /**
     * Write minimal error row when original data is not available
     */
    private void writeErrorRowMinimal(PrintWriter writer, BatchError error) {
        // LENDER (empty)
        writer.write(CSV_DELIMITER);
        // ACCOUNT NO (external case id / loan id)
        writer.write(escapeCsv(error.getExternalCaseId()));
        writer.write(CSV_DELIMITER);

        // Columns 3-86 (84 empty columns)
        for (int i = 0; i < 84; i++) {
            writer.write(CSV_DELIMITER);
        }

        // STATUS - FAILURE for error records
        writer.write("FAILURE");
        writer.write(CSV_DELIMITER);
        // REMARKS - error details with row number
        writer.write(escapeCsv("Row " + error.getRowNumber() + ": " + error.getErrorMessage()));
        writer.write(LINE_SEPARATOR);
    }

    /**
     * Get string value from JSON node, returns empty string if null
     */
    private String getJsonString(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return "";
        }
        return field.asText();
    }
}

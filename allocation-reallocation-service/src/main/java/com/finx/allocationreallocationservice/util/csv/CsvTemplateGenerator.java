package com.finx.allocationreallocationservice.util.csv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for generating CSV templates for allocation operations
 */
@Slf4j
@Component
public class CsvTemplateGenerator {

    /**
     * Generate allocation CSV template
     * Headers match CaseCsvRowDTO.java from case-sourcing-service for consistency
     * Total: 86 columns matching case upload template
     */
    public byte[] generateAllocationTemplate(boolean includeSample) {
        // All columns matching case-sourcing-service CaseCsvRowDTO.java in exact order
        List<String> headers = List.of(
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
                "PRIMARY AGENT", "SECONDARY AGENT", "REALLOCATE TO AGENT",
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
                "AGENCY NAME");

        if (!includeSample) {
            return generateTemplate(headers, null);
        }

        // Sample row with all 86 columns matching case upload template
        List<List<String>> sampleRows = List.of(
                List.of(
                        // Lender & Account
                        "ABC Finance", "LA123456789",
                        // Customer Information
                        "Naveen Kumar", "9398365948", "CUST001", "naveen@email.com",
                        "9876543210", "04024567890", "9123456780",
                        // Address
                        "123 Main Street Andheri", "456 Sub Road Andheri East", "Mumbai", "Maharashtra", "400069",
                        // Loan Financial Details
                        "125000", "95000", "120000", "500000", "15000",
                        "5000", "2000", "3000",
                        // Overdue Breakdown
                        "80000", "30000", "10000", "5000",
                        // EMI Details
                        "2024-01-15", "10", "14", "2024-10-15", "2025-01-15",
                        // Loan Tenure
                        "24 months", "12.5",
                        // Important Dates
                        "2024-01-15", "2026-01-15", "2024-12-05", "",
                        // DPD & Bucket
                        "45", "X", "B1", "30", "2",
                        // Product & Scheme
                        "Personal Loan", "PL001", "DSA",
                        // Credit Card Specific
                        "", "", "", "", "", "", "",
                        // Payment Information
                        "2024-11-01", "NEFT", "12000",
                        // Repayment Bank Details
                        "1234567890", "Naveen Kumar", "HDFC Bank", "HDFC0001234", "https://pay.finx.com/123",
                        // Lender References
                        "XYZ Bank", "",
                        // Family & Employment
                        "Ramesh Kumar", "ABC Corp",
                        // References
                        "Suresh", "9876543211", "Priya", "9876543212",
                        // Block Status
                        "", "", "", "",
                        // Location & Geography
                        "Mumbai West", "WEST", "en",
                        // Agent Allocation (PRIMARY AGENT, SECONDARY AGENT, REALLOCATE TO AGENT)
                        "101", "102", "",
                        // Sourcing
                        "Rahul Sales",
                        // Flags
                        "",
                        // Asset Details
                        "", "", "", "", "", "",
                        // Dealer
                        "", "",
                        // Agency
                        ""));

        return generateTemplate(headers, sampleRows);
    }

    /**
     * Generate contact update CSV template
     * Headers match ContactUpdateCsvRow.java annotations
     * Uses loan_id for consistency with case sourcing and allocation CSVs
     */
    public byte[] generateContactUpdateTemplate(boolean includeSample, String updateType) {
        if (updateType == null || updateType.isEmpty()) {
            updateType = "MOBILE_UPDATE";
        }

        List<String> headers;
        List<List<String>> sampleRows = null;

        switch (updateType.toUpperCase()) {
            case "MOBILE_UPDATE":
                headers = List.of(
                        "loan_id",
                        "update_type",
                        "mobile_number",
                        "alternate_mobile",
                        "remarks");
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("LA123456789", "MOBILE_UPDATE", "9999999999", "8888888888", "New mobile verified"),
                            List.of("LA987654321", "MOBILE_UPDATE", "7777777777", "", "Updated from bank"));
                }
                break;

            case "EMAIL_UPDATE":
                headers = List.of(
                        "loan_id",
                        "update_type",
                        "email",
                        "alternate_email",
                        "remarks");
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("LA123456789", "EMAIL_UPDATE", "newemail@example.com", "alt@example.com",
                                    "Email verified"),
                            List.of("LA987654321", "EMAIL_UPDATE", "updated@example.com", "", "Borrower provided"));
                }
                break;

            case "ADDRESS_UPDATE":
                headers = List.of(
                        "loan_id",
                        "update_type",
                        "address",
                        "city",
                        "state",
                        "pincode",
                        "remarks");
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("LA123456789", "ADDRESS_UPDATE", "123 New Street", "Mumbai", "Maharashtra",
                                    "400001", "Verified address"),
                            List.of("LA987654321", "ADDRESS_UPDATE", "456 Updated Avenue", "Delhi", "Delhi", "110001",
                                    "Field visit confirmed"));
                }
                break;

            default:
                // Full template with all fields
                headers = List.of(
                        "loan_id",
                        "update_type",
                        "mobile_number",
                        "alternate_mobile",
                        "email",
                        "alternate_email",
                        "address",
                        "city",
                        "state",
                        "pincode",
                        "remarks");
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("LA123456789", "MOBILE_UPDATE", "9999999999", "8888888888", "", "", "", "", "", "",
                                    "Mobile update"),
                            List.of("LA987654321", "EMAIL_UPDATE", "", "", "email@example.com", "", "", "", "", "",
                                    "Email update"),
                            List.of("LA555555555", "ADDRESS_UPDATE", "", "", "", "", "123 Street", "Mumbai", "MH",
                                    "400001", "Address update"));
                }
        }

        return generateTemplate(headers, sampleRows);
    }

    /**
     * Generate reallocation CSV template
     * Headers match ReallocationCsvRow.java annotations
     */
    public byte[] generateReallocationTemplate(boolean includeSample) {
        List<String> headers = List.of(
                "case_id",
                "external_case_id",
                "loan_account_number",
                "current_agent_id",
                "new_agent_id",
                "reallocation_reason",
                "reallocation_type",
                "effective_date",
                "priority",
                "remarks");

        if (!includeSample) {
            return generateTemplate(headers, null);
        }

        List<List<String>> sampleRows = List.of(
                List.of(
                        "1001",
                        "EXT001",
                        "LA123456789",
                        "101",
                        "105",
                        "Workload balancing",
                        "IMMEDIATE",
                        "2025-11-20",
                        "HIGH",
                        "Agent 101 at full capacity"),
                List.of(
                        "1002",
                        "EXT002",
                        "LA987654321",
                        "102",
                        "105",
                        "Agent resignation",
                        "IMMEDIATE",
                        "2025-11-20",
                        "HIGH",
                        "Agent 102 resigned"));

        return generateTemplate(headers, sampleRows);
    }

    /**
     * Generate CSV template with headers and optional sample rows
     */
    private byte[] generateTemplate(List<String> headers, List<List<String>> sampleRows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            // Write header row
            writer.write(String.join(",", headers));
            writer.write("\n");

            // Write sample rows if provided
            if (sampleRows != null && !sampleRows.isEmpty()) {
                for (List<String> row : sampleRows) {
                    // Escape each value properly for CSV format
                    List<String> escapedRow = row.stream()
                            .map(this::escapeCsvValue)
                            .toList();
                    writer.write(String.join(",", escapedRow));
                    writer.write("\n");
                }
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV template", e);
            throw new RuntimeException("Failed to generate CSV template", e);
        }
    }

    /**
     * Escape CSV value (handle commas, quotes, newlines)
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape
        // existing quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}

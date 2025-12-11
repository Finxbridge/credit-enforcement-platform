package com.finx.casesourcingservice.util.csv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for generating CSV templates with headers and optional sample data
 */
@Slf4j
@Component
public class CsvTemplateGenerator {

    /**
     * Generate CSV template with headers only
     */
    public byte[] generateTemplate(List<String> headers) {
        return generateTemplate(headers, null);
    }

    /**
     * Generate CSV template with headers and sample rows
     *
     * @param headers      List of column headers
     * @param sampleRows   List of sample data rows (each row is a list of values)
     * @return CSV file as byte array
     */
    public byte[] generateTemplate(List<String> headers, List<List<String>> sampleRows) {
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
     * Generate case upload CSV template
     * Headers match CaseCsvRowDTO.java @CsvBindByName annotations exactly
     * Total: 88 columns (including STATUS and REMARKS for export)
     */
    public byte[] generateCaseUploadTemplate(boolean includeSample) {
        // All 88 columns matching CaseCsvRowDTO.java in exact order
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
                "AGENCY NAME"
        );

        if (!includeSample) {
            return generateTemplate(headers);
        }

        // Sample row with all 86 columns (excluding STATUS and REMARKS which are for export only)
        List<List<String>> sampleRows = List.of(
                List.of(
                        // Lender & Account
                        "ABC Finance", "LA123456789",
                        // Customer Information
                        "Naveen Kumar", "9398365948", "CUST001", "naveen@email.com",
                        "9876543210", "04024567890", "9123456780",
                        // Address (escapeCsvValue will handle quoting if needed)
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
                        "Mumbai West", "WEST", "En_US",
                        // Agent Allocation (PRIMARY AGENT, SECONDARY AGENT, REALLOCATE TO AGENT)
                        "", "", "",
                        // Sourcing
                        "Rahul Sales",
                        // Flags
                        "",
                        // Asset Details
                        "", "", "", "", "", "",
                        // Dealer
                        "", "",
                        // Agency
                        ""
                )
        );

        return generateTemplate(headers, sampleRows);
    }

    /**
     * Escape CSV value (handle commas, quotes, newlines)
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape existing quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}

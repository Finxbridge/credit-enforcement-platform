package com.finx.casesourcingservice.util.csv;

import com.finx.casesourcingservice.domain.entity.BatchError;
import com.finx.casesourcingservice.domain.entity.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for exporting data to CSV format
 */
@Slf4j
@Component
public class CsvExporter {

    private static final String CSV_DELIMITER = ",";
    private static final String LINE_SEPARATOR = "\n";

    /**
     * Export failed cases (batch errors) to CSV
     */
    public byte[] exportBatchErrors(List<BatchError> errors) {
        log.info("Exporting {} batch errors to CSV", errors.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header
        writer.write("Row Number,External Case ID,Error Type,Error Message");
        writer.write(LINE_SEPARATOR);

        // Write data rows
        for (BatchError error : errors) {
            writer.write(String.valueOf(error.getRowNumber()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(error.getExternalCaseId()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(error.getErrorType().name()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(error.getErrorMessage()));
            writer.write(LINE_SEPARATOR);
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Export cases to CSV
     * FORMAT: Compatible with allocation service upload
     * User workflow: Download from case-sourcing → Edit (agents, status, remarks) → Upload to allocation
     */
    public byte[] exportCases(List<Case> cases) {
        log.info("Exporting {} cases to CSV", cases.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header - MATCHES allocation upload format exactly
        writer.write("loan_id,case_number,customer_name,outstanding,dpd,geography,bucket,");
        writer.write("primary_agent_id,secondary_agent_id,allocation_type,allocation_percentage,priority,remarks");
        writer.write(LINE_SEPARATOR);

        // Write data rows
        for (Case caseEntity : cases) {
            // loan_id (required for allocation)
            writer.write(escapeCsv(caseEntity.getLoan().getLoanAccountNumber()));
            writer.write(CSV_DELIMITER);

            // case_number (reference for user)
            writer.write(escapeCsv(caseEntity.getCaseNumber()));
            writer.write(CSV_DELIMITER);

            // customer_name (reference for user)
            writer.write(escapeCsv(caseEntity.getLoan().getPrimaryCustomer().getFullName()));
            writer.write(CSV_DELIMITER);

            // outstanding (reference)
            writer.write(String.valueOf(caseEntity.getLoan().getTotalOutstanding()));
            writer.write(CSV_DELIMITER);

            // dpd (reference)
            writer.write(String.valueOf(caseEntity.getLoan().getDpd()));
            writer.write(CSV_DELIMITER);

            // geography (from case - user can edit)
            writer.write(escapeCsv(caseEntity.getGeographyCode()));
            writer.write(CSV_DELIMITER);

            // bucket (from loan - user can edit)
            writer.write(escapeCsv(caseEntity.getLoan().getBucket()));
            writer.write(CSV_DELIMITER);

            // primary_agent_id (user will fill this)
            writer.write(caseEntity.getAllocatedToUserId() != null ?
                    String.valueOf(caseEntity.getAllocatedToUserId()) : "");
            writer.write(CSV_DELIMITER);

            // secondary_agent_id (user will fill this)
            writer.write("");
            writer.write(CSV_DELIMITER);

            // allocation_type (user can edit: PRIMARY, DUAL, etc.)
            writer.write("PRIMARY");
            writer.write(CSV_DELIMITER);

            // allocation_percentage (user can edit)
            writer.write("100");
            writer.write(CSV_DELIMITER);

            // priority (user can edit: HIGH, MEDIUM, LOW, CRITICAL)
            writer.write(escapeCsv(caseEntity.getCasePriority() != null ?
                    caseEntity.getCasePriority() : "MEDIUM"));
            writer.write(CSV_DELIMITER);

            // remarks (user can edit/add notes)
            writer.write("");
            writer.write(LINE_SEPARATOR);
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
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
}

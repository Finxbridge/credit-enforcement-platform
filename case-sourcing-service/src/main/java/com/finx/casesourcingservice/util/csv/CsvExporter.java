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
     */
    public byte[] exportCases(List<Case> cases) {
        log.info("Exporting {} cases to CSV", cases.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);

        // Write header
        writer.write("Case Number,External Case ID,Customer Name,Mobile,Loan Account,");
        writer.write("Total Outstanding,DPD,Bucket,Status,Allocated To User,Created At");
        writer.write(LINE_SEPARATOR);

        // Write data rows
        for (Case caseEntity : cases) {
            writer.write(escapeCsv(caseEntity.getCaseNumber()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(caseEntity.getExternalCaseId()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(caseEntity.getLoan().getPrimaryCustomer().getFullName()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(caseEntity.getLoan().getPrimaryCustomer().getMobileNumber()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(caseEntity.getLoan().getLoanAccountNumber()));
            writer.write(CSV_DELIMITER);
            writer.write(String.valueOf(caseEntity.getLoan().getTotalOutstanding()));
            writer.write(CSV_DELIMITER);
            writer.write(String.valueOf(caseEntity.getLoan().getDpd()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(caseEntity.getLoan().getBucket()));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(caseEntity.getCaseStatus()));
            writer.write(CSV_DELIMITER);
            writer.write(caseEntity.getAllocatedToUserId() != null ?
                    String.valueOf(caseEntity.getAllocatedToUserId()) : "");
            writer.write(CSV_DELIMITER);
            writer.write(String.valueOf(caseEntity.getCreatedAt()));
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

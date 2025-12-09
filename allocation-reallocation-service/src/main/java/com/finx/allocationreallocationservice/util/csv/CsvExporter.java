package com.finx.allocationreallocationservice.util.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.allocationreallocationservice.domain.entity.BatchError;
import com.finx.allocationreallocationservice.domain.entity.CaseAllocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV Exporter for allocation-reallocation service
 * Uses same column format as AllocationCsvRow.java for consistency
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvExporter {

    private final ObjectMapper objectMapper;

    private static final String CSV_DELIMITER = ",";
    private static final String LINE_SEPARATOR = "\n";

    /**
     * Header matching AllocationCsvRow.java exactly (15 columns + STATUS + REMARKS)
     */
    private static final String ALLOCATION_HEADER = String.join(CSV_DELIMITER,
            "case_id",
            "external_case_id",
            "loan_id",
            "case_number",
            "customer_name",
            "outstanding",
            "dpd",
            "primary_agent_id",
            "secondary_agent_id",
            "allocation_type",
            "allocation_percentage",
            "geography",
            "bucket",
            "priority",
            "remarks",
            "STATUS",
            "REMARKS"
    );

    /**
     * Export batch errors to CSV with same format as allocation template
     * Only includes error records (for Error Export functionality)
     */
    public byte[] exportBatchErrors(List<BatchError> errors) {
        log.info("Exporting {} batch errors to CSV", errors.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8)) {
            // Write header (17 columns)
            writer.write(ALLOCATION_HEADER);
            writer.write(LINE_SEPARATOR);

            // Write data rows - fill only the fields we have
            for (BatchError error : errors) {
                // case_id
                writer.write("");
                writer.write(CSV_DELIMITER);
                // external_case_id
                writer.write(escapeCsv(error.getExternalCaseId()));
                writer.write(CSV_DELIMITER);
                // loan_id through priority (12 empty columns)
                for (int i = 0; i < 12; i++) {
                    writer.write(CSV_DELIMITER);
                }
                // remarks (from original data - empty)
                writer.write("");
                writer.write(CSV_DELIMITER);
                // STATUS - always FAILURE for error export
                writer.write("FAILURE");
                writer.write(CSV_DELIMITER);
                // REMARKS (error details)
                writer.write(escapeCsv("Row " + error.getRowNumber() + ": " + error.getErrorMessage()));
                writer.write(LINE_SEPARATOR);
            }

            writer.flush();
        }

        return outputStream.toByteArray();
    }

    /**
     * Export ALL case allocations to CSV (success + error)
     * Uses same format as allocation template for re-upload capability
     */
    public byte[] exportAllocations(List<CaseAllocation> allocations) {
        log.info("Exporting {} case allocations to CSV", allocations.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8)) {
            // Write header (17 columns)
            writer.write(ALLOCATION_HEADER);
            writer.write(LINE_SEPARATOR);

            // Write data rows
            for (CaseAllocation allocation : allocations) {
                writeAllocationRow(writer, allocation, null, null);
            }

            writer.flush();
        }

        return outputStream.toByteArray();
    }

    /**
     * Export allocations with custom status and remarks
     * Used for status reports or filtered exports
     */
    public byte[] exportAllocationsWithStatus(List<CaseAllocation> allocations,
                                               List<String> statuses, List<String> remarksList) {
        log.info("Exporting {} case allocations with status to CSV", allocations.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8)) {
            // Write header (17 columns)
            writer.write(ALLOCATION_HEADER);
            writer.write(LINE_SEPARATOR);

            // Write data rows
            for (int i = 0; i < allocations.size(); i++) {
                String status = (statuses != null && i < statuses.size()) ? statuses.get(i) : null;
                String remark = (remarksList != null && i < remarksList.size()) ? remarksList.get(i) : null;
                writeAllocationRow(writer, allocations.get(i), status, remark);
            }

            writer.flush();
        }

        return outputStream.toByteArray();
    }

    /**
     * Write a single allocation row with all 17 columns
     */
    private void writeAllocationRow(PrintWriter writer, CaseAllocation allocation,
                                     String customStatus, String customRemark) {
        // case_id
        writer.write(allocation.getCaseId() != null ? String.valueOf(allocation.getCaseId()) : "");
        writer.write(CSV_DELIMITER);

        // external_case_id
        writer.write(escapeCsv(allocation.getExternalCaseId()));
        writer.write(CSV_DELIMITER);

        // loan_id (from external case id or empty)
        writer.write(escapeCsv(allocation.getExternalCaseId())); // Often loan_id = external_case_id
        writer.write(CSV_DELIMITER);

        // case_number (empty - reference field)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // customer_name (empty - reference field)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // outstanding (empty - reference field)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // dpd (empty - reference field)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // primary_agent_id
        writer.write(allocation.getPrimaryAgentId() != null ?
                String.valueOf(allocation.getPrimaryAgentId()) : "");
        writer.write(CSV_DELIMITER);

        // secondary_agent_id
        writer.write(allocation.getSecondaryAgentId() != null ?
                String.valueOf(allocation.getSecondaryAgentId()) : "");
        writer.write(CSV_DELIMITER);

        // allocation_type
        writer.write(escapeCsv(allocation.getAllocationType()));
        writer.write(CSV_DELIMITER);

        // allocation_percentage
        writer.write(allocation.getWorkloadPercentage() != null ?
                allocation.getWorkloadPercentage().toPlainString() : "100");
        writer.write(CSV_DELIMITER);

        // geography
        writer.write(escapeCsv(allocation.getGeographyCode()));
        writer.write(CSV_DELIMITER);

        // bucket (empty)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // priority (empty)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // remarks (original)
        writer.write("");
        writer.write(CSV_DELIMITER);

        // STATUS - SUCCESS/FAILURE instead of allocation status
        String status = customStatus != null ? customStatus : "SUCCESS";
        writer.write(escapeCsv(status));
        writer.write(CSV_DELIMITER);

        // REMARKS
        writer.write(escapeCsv(customRemark != null ? customRemark : ""));
        writer.write(LINE_SEPARATOR);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    /**
     * Export ALL batch data - both successful allocations and errors combined
     * Used for "Export Cases" which should include both success and failure records
     * SUCCESS allocations with STATUS="SUCCESS", errors with STATUS="FAILURE"
     */
    public byte[] exportAllBatchData(List<CaseAllocation> allocations, List<BatchError> errors) {
        log.info("Exporting batch data: {} success allocations, {} errors",
                allocations != null ? allocations.size() : 0,
                errors != null ? errors.size() : 0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8)) {
            // Write header (17 columns)
            writer.write(ALLOCATION_HEADER);
            writer.write(LINE_SEPARATOR);

            // Write successful allocations with STATUS="SUCCESS"
            if (allocations != null) {
                for (CaseAllocation allocation : allocations) {
                    writeAllocationRow(writer, allocation, "SUCCESS", "");
                }
            }

            // Write error records with STATUS="FAILURE"
            if (errors != null) {
                for (BatchError error : errors) {
                    writeErrorRow(writer, error);
                }
            }

            writer.flush();
        }

        return outputStream.toByteArray();
    }

    /**
     * Write a single error row with all 17 columns
     * Uses original row data (JSON) if available, otherwise falls back to minimal data
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

            // Write all 15 data columns from JSON (matching AllocationCsvRow field names)
            writer.write(escapeCsv(getJsonString(node, "caseId")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "externalCaseId")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "loanId")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "caseNumber")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "customerName")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "outstanding")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "dpd")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "primaryAgentId")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "secondaryAgentId")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "allocationType")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "allocationPercentage")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "geography")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "bucket")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "priority")));
            writer.write(CSV_DELIMITER);
            writer.write(escapeCsv(getJsonString(node, "remarks")));
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
        // case_id (empty)
        writer.write("");
        writer.write(CSV_DELIMITER);
        // external_case_id
        writer.write(escapeCsv(error.getExternalCaseId()));
        writer.write(CSV_DELIMITER);
        // loan_id through priority (12 empty columns)
        for (int i = 0; i < 12; i++) {
            writer.write(CSV_DELIMITER);
        }
        // remarks (empty)
        writer.write("");
        writer.write(CSV_DELIMITER);
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

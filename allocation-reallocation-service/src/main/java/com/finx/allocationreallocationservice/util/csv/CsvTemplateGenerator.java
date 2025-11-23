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
     * Headers match AllocationCsvRow.java annotations
     * NOTE: loan_id is REQUIRED (not case_id) - users know loan IDs from case upload, not database IDs
     */
    public byte[] generateAllocationTemplate(boolean includeSample) {
        List<String> headers = List.of(
                "loan_id",
                "primary_agent_id",
                "secondary_agent_id",
                "allocation_type",
                "allocation_percentage",
                "geography",
                "bucket",
                "priority",
                "remarks"
        );

        if (!includeSample) {
            return generateTemplate(headers, null);
        }

        List<List<String>> sampleRows = List.of(
                List.of(
                        "LA123456789",
                        "101",
                        "102",
                        "PRIMARY",
                        "100",
                        "MUMBAI_WEST",
                        "X",
                        "HIGH",
                        "High value case"
                ),
                List.of(
                        "LA987654321",
                        "103",
                        "",
                        "PRIMARY",
                        "100",
                        "DELHI_SOUTH",
                        "M2",
                        "MEDIUM",
                        ""
                )
        );

        return generateTemplate(headers, sampleRows);
    }

    /**
     * Generate contact update CSV template
     * Headers match ContactUpdateCsvRow.java annotations
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
                        "case_id",
                        "update_type",
                        "mobile_number",
                        "alternate_mobile",
                        "remarks"
                );
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("1001", "MOBILE_UPDATE", "9999999999", "8888888888", "New mobile verified"),
                            List.of("1002", "MOBILE_UPDATE", "7777777777", "", "Updated from bank")
                    );
                }
                break;

            case "EMAIL_UPDATE":
                headers = List.of(
                        "case_id",
                        "update_type",
                        "email",
                        "alternate_email",
                        "remarks"
                );
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("1001", "EMAIL_UPDATE", "newemail@example.com", "alt@example.com", "Email verified"),
                            List.of("1002", "EMAIL_UPDATE", "updated@example.com", "", "Borrower provided")
                    );
                }
                break;

            case "ADDRESS_UPDATE":
                headers = List.of(
                        "case_id",
                        "update_type",
                        "address",
                        "city",
                        "state",
                        "pincode",
                        "remarks"
                );
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("1001", "ADDRESS_UPDATE", "123 New Street", "Mumbai", "Maharashtra", "400001", "Verified address"),
                            List.of("1002", "ADDRESS_UPDATE", "456 Updated Avenue", "Delhi", "Delhi", "110001", "Field visit confirmed")
                    );
                }
                break;

            default:
                // Full template with all fields
                headers = List.of(
                        "case_id",
                        "update_type",
                        "mobile_number",
                        "alternate_mobile",
                        "email",
                        "alternate_email",
                        "address",
                        "city",
                        "state",
                        "pincode",
                        "remarks"
                );
                if (includeSample) {
                    sampleRows = List.of(
                            List.of("1001", "MOBILE_UPDATE", "9999999999", "8888888888", "", "", "", "", "", "", "Mobile update"),
                            List.of("1002", "EMAIL_UPDATE", "", "", "email@example.com", "", "", "", "", "", "Email update"),
                            List.of("1003", "ADDRESS_UPDATE", "", "", "", "", "123 Street", "Mumbai", "MH", "400001", "Address update")
                    );
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
                "remarks"
        );

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
                        "Agent 101 at full capacity"
                ),
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
                        "Agent 102 resigned"
                )
        );

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
                    writer.write(String.join(",", row));
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
}

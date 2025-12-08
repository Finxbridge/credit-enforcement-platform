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

    /**
     * Generate case upload CSV template
     * Headers match CaseCsvRowDTO.java annotations (required fields only)
     */
    public byte[] generateCaseUploadTemplate(boolean includeSample) {
        List<String> headers = List.of(
                "case_id",
                "loan_id",
                "customer_code",
                "customer_name",
                "phone",
                "geography",
                "language",
                "outstanding",
                "dpd",
                "primary_agent",
                "secondary_agent"
        );

        if (!includeSample) {
            return generateTemplate(headers);
        }

        List<List<String>> sampleRows = List.of(
                List.of(
                        "EXT001",
                        "LA123456789",
                        "CUST001",
                        "John Doe",
                        "9999999999",
                        "MUMBAI_WEST",
                        "en",
                        "125000",
                        "45",
                        "101",
                        "102"
                ),
                List.of(
                        "EXT002",
                        "LA987654321",
                        "CUST002",
                        "Jane Smith",
                        "8888888888",
                        "DELHI_SOUTH",
                        "hi",
                        "180000",
                        "32",
                        "103",
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

package com.finx.allocationreallocationservice.util.csv;

import com.finx.allocationreallocationservice.domain.entity.BatchError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class CsvExporter {

    private static final String CSV_DELIMITER = ",";
    private static final String LINE_SEPARATOR = "\n";

    public byte[] exportBatchErrors(List<BatchError> errors) {
        log.info("Exporting {} batch errors to CSV", errors.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8)) {
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
        }

        return outputStream.toByteArray();
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
}

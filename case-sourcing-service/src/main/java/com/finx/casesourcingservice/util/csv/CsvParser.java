package com.finx.casesourcingservice.util.csv;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for parsing CSV files for case uploads
 */
@Slf4j
@Component
public class CsvParser {

    private static final String CSV_DELIMITER = ",";

    /**
     * Parse case upload CSV file
     */
    public List<CaseCsvRowDTO> parseCaseCsv(Path filePath) {
        List<CaseCsvRowDTO> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {

            // Skip header row
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException("CSV file is empty");
            }

            log.info("CSV Headers: {}", headerLine);

            String line;
            int rowNumber = 1; // Start from 1 (after header)

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    CaseCsvRowDTO row = parseCsvRow(line, rowNumber);
                    rows.add(row);
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", rowNumber, e.getMessage());
                    // Create a row with error for tracking
                    CaseCsvRowDTO errorRow = CaseCsvRowDTO.builder()
                            .rowNumber(rowNumber)
                            .build();
                    rows.add(errorRow);
                }
            }

            log.info("Parsed {} rows from CSV", rows.size());

        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", e.getMessage(), e);
            throw new BusinessException("Failed to parse CSV file: " + e.getMessage());
        }

        return rows;
    }

    /**
     * Parse a single CSV row
     * Expected columns: externalCaseId, loanAccountNumber, customerCode, fullName,
     * mobileNumber,
     * alternateMobile, email, address, city, state, pincode, principalAmount,
     * interestAmount,
     * penaltyAmount, totalOutstanding, emiAmount, dpd, bucket, productType,
     * productCode,
     * bankCode, disbursementDate, dueDate
     */
    private CaseCsvRowDTO parseCsvRow(String line, int rowNumber) {
        String[] columns = line.split(CSV_DELIMITER, -1); // -1 to include trailing empty values

        return CaseCsvRowDTO.builder()
                .rowNumber(rowNumber)
                .externalCaseId(getColumn(columns, 0))
                .loanAccountNumber(getColumn(columns, 1))
                .customerCode(getColumn(columns, 2))
                .fullName(getColumn(columns, 3))
                .mobileNumber(getColumn(columns, 4))
                .alternateMobile(getColumn(columns, 5))
                .email(getColumn(columns, 6))
                .address(getColumn(columns, 7))
                .city(getColumn(columns, 8))
                .state(getColumn(columns, 9))
                .pincode(getColumn(columns, 10))
                .principalAmount(parseBigDecimal(getColumn(columns, 11)))
                .interestAmount(parseBigDecimal(getColumn(columns, 12)))
                .penaltyAmount(parseBigDecimal(getColumn(columns, 13)))
                .totalOutstanding(parseBigDecimal(getColumn(columns, 14)))
                .emiAmount(parseBigDecimal(getColumn(columns, 15)))
                .dpd(parseInteger(getColumn(columns, 16)))
                .bucket(getColumn(columns, 17))
                .productType(getColumn(columns, 18))
                .productCode(getColumn(columns, 19))
                .bankCode(getColumn(columns, 20))
                .disbursementDate(getColumn(columns, 21))
                .dueDate(getColumn(columns, 22))
                .build();
    }

    private String getColumn(String[] columns, int index) {
        if (index >= columns.length) {
            return null;
        }
        String value = columns[index].trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid BigDecimal value: {}", value);
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid Integer value: {}", value);
            return null;
        }
    }
}

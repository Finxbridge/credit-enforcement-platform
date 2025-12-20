package com.finx.casesourcingservice.util.csv;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.exception.BusinessException;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility for parsing CSV files for case uploads using OpenCSV
 */
@Slf4j
@Component
public class CsvParser {

    /**
     * Parse case upload CSV file using OpenCSV with @CsvBindByName annotations.
     * Configured to handle fields containing commas by properly respecting quoted values.
     */
    public List<CaseCsvRowDTO> parseCaseCsv(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {

            // Configure CSV parser to properly handle quoted fields with commas
            var csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withIgnoreQuotations(false)
                    .withStrictQuotes(false)
                    .build();

            // Build CSVReader with the custom parser
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .build();

            CsvToBean<CaseCsvRowDTO> csvToBean = new CsvToBeanBuilder<CaseCsvRowDTO>(csvReader)
                    .withType(CaseCsvRowDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)  // Collect exceptions instead of throwing immediately
                    .build();

            List<CaseCsvRowDTO> rows = csvToBean.parse();

            // Log any captured exceptions for debugging
            var exceptions = csvToBean.getCapturedExceptions();
            if (!exceptions.isEmpty()) {
                log.warn("CSV parsing completed with {} warnings/errors", exceptions.size());
                exceptions.forEach(ex -> log.warn("CSV parsing issue at line {}: {}",
                        ex.getLineNumber(), ex.getMessage()));
            }

            // Add row numbers for tracking
            AtomicInteger rowNumber = new AtomicInteger(1);
            rows.forEach(row -> row.setRowNumber(rowNumber.getAndIncrement()));

            log.info("Parsed {} rows from CSV using OpenCSV", rows.size());
            return rows;

        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", e.getMessage(), e);
            throw new BusinessException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}

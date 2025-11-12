package com.finx.casesourcingservice.util.csv;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.exception.BusinessException;
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
     * Parse case upload CSV file using OpenCSV with @CsvBindByName annotations
     */
    public List<CaseCsvRowDTO> parseCaseCsv(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {

            CsvToBean<CaseCsvRowDTO> csvToBean = new CsvToBeanBuilder<CaseCsvRowDTO>(reader)
                    .withType(CaseCsvRowDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<CaseCsvRowDTO> rows = csvToBean.parse();

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

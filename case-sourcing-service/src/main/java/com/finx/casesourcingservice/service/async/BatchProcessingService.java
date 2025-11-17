package com.finx.casesourcingservice.service.async;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.domain.dto.csv.CaseValidationResult;
import com.finx.casesourcingservice.domain.entity.*;
import com.finx.casesourcingservice.domain.enums.BatchStatus;
import com.finx.casesourcingservice.domain.enums.ErrorType;
import com.finx.casesourcingservice.repository.*;
import com.finx.casesourcingservice.service.CaseValidationService;
import com.finx.casesourcingservice.util.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Async service for processing case upload batches
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessingService {

    private final CsvParser csvParser;
    private final CaseValidationService validationService;
    private final CaseBatchRepository caseBatchRepository;
    private final BatchErrorRepository batchErrorRepository;
    private final CustomerRepository customerRepository;
    private final LoanDetailsRepository loanDetailsRepository;
    private final CaseRepository caseRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Process batch asynchronously
     */
    @Async("batchProcessingExecutor")
    public void processBatchAsync(String batchId, String filePath) {
        log.info("Starting async processing for batch: {}", batchId);
        Path path = Paths.get(filePath);

        try {
            // Update batch status to PROCESSING
            CaseBatch batch = caseBatchRepository.findByBatchId(batchId)
                    .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));

            // Parse CSV
            List<CaseCsvRowDTO> rows = csvParser.parseCaseCsv(path);
            batch.setTotalCases(rows.size());
            caseBatchRepository.save(batch);

            int validCount = 0;
            int invalidCount = 0;
            int duplicateCount = 0;

            // Process each row
            for (CaseCsvRowDTO row : rows) {
                try {
                    // Validate row
                    CaseValidationResult validationResult = validationService.validateCaseRow(row);

                    if (validationResult.isValid()) {
                        // Create case in separate transaction
                        processSingleRow(row, batchId);
                        validCount++;
                    } else {
                        // Log errors
                        for (String error : validationResult.getErrors()) {
                            logBatchErrorInNewTransaction(batchId, row, error);

                            // Check if it's a duplicate error
                            if (error.contains("Duplicate")) {
                                duplicateCount++;
                            }
                        }
                        invalidCount++;
                    }
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    log.error("Database constraint violation for row {}: {}", row.getRowNumber(), e.getMessage());
                    String errorMsg = "Database constraint violation: ";
                    if (e.getMessage().contains("loan_account_number")) {
                        errorMsg += "Duplicate loan account number: " + row.getLoanAccountNumber();
                        duplicateCount++;
                    } else if (e.getMessage().contains("external_case_id")) {
                        errorMsg += "Duplicate external case ID: " + row.getExternalCaseId();
                        duplicateCount++;
                    } else {
                        errorMsg += e.getMessage();
                    }
                    logBatchErrorInNewTransaction(batchId, row, errorMsg);
                    invalidCount++;
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", row.getRowNumber(), e.getMessage(), e);
                    logBatchErrorInNewTransaction(batchId, row, "System error: " + e.getMessage());
                    invalidCount++;
                }
            }

            // Update batch with final counts
            batch.setValidCases(validCount);
            batch.setInvalidCases(invalidCount);
            batch.setDuplicateCases(duplicateCount);
            batch.setStatus(BatchStatus.COMPLETED);
            batch.setCompletedAt(LocalDateTime.now());
            caseBatchRepository.save(batch);

            log.info("Batch {} processing completed. Valid: {}, Invalid: {}, Duplicates: {}",
                    batchId, validCount, invalidCount, duplicateCount);

        } catch (Exception e) {
            log.error("Fatal error processing batch {}: {}", batchId, e.getMessage(), e);

            // Update batch status to FAILED
            caseBatchRepository.findByBatchId(batchId).ifPresent(batch -> {
                batch.setStatus(BatchStatus.FAILED);
                batch.setCompletedAt(LocalDateTime.now());
                caseBatchRepository.save(batch);
            });
        } finally {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("Failed to delete temporary file: {}", filePath, e);
            }
        }
    }

    /**
     * Process a single row in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleRow(CaseCsvRowDTO row, String batchId) {
        createCase(row, batchId);
    }

    @SuppressWarnings("null")
    private void createCase(CaseCsvRowDTO row, String batchId) {
        // Create or get customer
        Customer customer = createOrGetCustomer(row);

        // Create loan details
        LoanDetails loanDetails = createLoanDetails(row, customer);

        // Create case
        Case caseEntity = Case.builder()
                .caseNumber(generateCaseNumber())
                .externalCaseId(row.getExternalCaseId())
                .loan(loanDetails)
                .caseStatus("UNALLOCATED")
                .casePriority("MEDIUM")
                .caseOpenedAt(LocalDateTime.now())
                .sourceType("MANUAL")
                .importBatchId(batchId)
                .geographyCode(row.getGeographyCode() != null ? row.getGeographyCode().toUpperCase() : null)
                .isArchived(false)
                .build();

        caseRepository.save(caseEntity);
    }

    @SuppressWarnings("null")
    private Customer createOrGetCustomer(CaseCsvRowDTO row) {
        return customerRepository.findByCustomerCode(row.getCustomerCode())
                .orElseGet(() -> {
                    // Customer not found, create a new one
                    Customer newCustomer = Customer.builder()
                            .customerCode(row.getCustomerCode())
                            .fullName(row.getFullName())
                            .mobileNumber(row.getMobileNumber())
                            .alternateMobile(row.getAlternateMobile())
                            .email(row.getEmail())
                            .address(row.getAddress())
                            .city(row.getCity())
                            .state(row.getState())
                            .pincode(row.getPincode())
                            .customerType("INDIVIDUAL")
                            .isActive(true)
                            .build();
                    return customerRepository.save(newCustomer);
                });
    }

    @SuppressWarnings("null")
    private LoanDetails createLoanDetails(CaseCsvRowDTO row, Customer customer) {
        return loanDetailsRepository.save(LoanDetails.builder()
                .loanAccountNumber(row.getLoanAccountNumber())
                .primaryCustomer(customer)
                .bankCode(row.getBankCode())
                .productCode(null) // Not available in CSV
                .productType(row.getProductType())
                .principalAmount(parseBigDecimal(row.getPrincipalAmount()))
                .interestAmount(null) // Not available in CSV
                .penaltyAmount(null) // Not available in CSV
                .totalOutstanding(parseBigDecimal(row.getTotalOutstanding()))
                .emiAmount(null) // Not available in CSV
                .dpd(parseInteger(row.getDpd()))
                .bucket(row.getBucket())
                .loanDisbursementDate(parseDate(row.getDisbursementDate()))
                .dueDate(parseDate(row.getDueDate()))
                .sourceSystem("CSV_UPLOAD")
                .build());
    }

    private java.math.BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal: {}", value);
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse Integer: {}", value);
            return null;
        }
    }

    /**
     * Log batch error in a new transaction
     */
    @SuppressWarnings("null")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBatchErrorInNewTransaction(String batchId, CaseCsvRowDTO row, String errorMessage) {
        ErrorType errorType = determineErrorType(errorMessage);

        batchErrorRepository.save(BatchError.builder()
                .batchId(batchId)
                .rowNumber(row.getRowNumber())
                .externalCaseId(row.getExternalCaseId())
                .errorType(errorType)
                .errorMessage(errorMessage)
                .build());
    }

    @SuppressWarnings("null")
    private void logBatchError(String batchId, CaseCsvRowDTO row, String errorMessage) {
        logBatchErrorInNewTransaction(batchId, row, errorMessage);
    }

    private ErrorType determineErrorType(String errorMessage) {
        if (errorMessage.contains("Duplicate")) {
            return ErrorType.DUPLICATE_ERROR;
        } else if (errorMessage.contains("required") || errorMessage.contains("Invalid")) {
            return ErrorType.VALIDATION_ERROR;
        } else if (errorMessage.contains("System error")) {
            return ErrorType.SYSTEM_ERROR;
        } else {
            return ErrorType.DATA_ERROR;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private String generateCaseNumber() {
        // Format: CASE-YYYY-NNNNNN
        String year = String.valueOf(LocalDate.now().getYear());
        long count = caseRepository.countTotalCases();
        return String.format("CASE-%s-%06d", year, count + 1);
    }
}

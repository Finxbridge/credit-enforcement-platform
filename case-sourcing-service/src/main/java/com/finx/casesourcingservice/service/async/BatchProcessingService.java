package com.finx.casesourcingservice.service.async;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.domain.dto.csv.CaseValidationResult;
import com.finx.casesourcingservice.domain.entity.*;
import com.finx.casesourcingservice.domain.enums.BatchStatus;
import com.finx.casesourcingservice.domain.enums.ErrorType;
import com.finx.casesourcingservice.repository.*;
import com.finx.casesourcingservice.service.CaseValidationService;
import com.finx.casesourcingservice.util.csv.CsvParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Async service for processing case upload batches
 * Updated to use unified CSV format field names
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
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
                        errorMsg += "Duplicate loan account number: " + row.getAccountNo();
                        duplicateCount++;
                    } else if (e.getMessage().contains("external_case_id")) {
                        errorMsg += "Duplicate external case ID: " + row.getAccountNo();
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
                .externalCaseId(row.getAccountNo()) // Use ACCOUNT NO as external case ID
                .loan(loanDetails)
                .caseStatus("UNALLOCATED")
                .casePriority("MEDIUM")
                .caseOpenedAt(LocalDateTime.now())
                .sourceType("MANUAL")
                .importBatchId(batchId)
                .location(row.getLocation())
                .zone(row.getZone())
                .primaryAgent(row.getPrimaryAgent())
                .secondaryAgent(row.getSecondaryAgent())
                .agencyName(row.getAgencyName())
                // Asset details
                .assetDetails(row.getAssetDetails())
                .vehicleRegistrationNumber(row.getVehicleRegistrationNumber())
                .vehicleIdentificationNumber(row.getVehicleIdentificationNumber())
                .chassisNumber(row.getChassisNumber())
                .modelMake(row.getModelMake())
                .batteryId(row.getBatteryId())
                // Dealer info
                .dealerName(row.getDealerName())
                .dealerAddress(row.getDealerAddress())
                // Flags
                .reviewFlag(row.getReviewFlag())
                .isArchived(false)
                .build();

        caseRepository.save(caseEntity);
    }

    @SuppressWarnings("null")
    private Customer createOrGetCustomer(CaseCsvRowDTO row) {
        // Use customerId or generate from mobile/name
        String customerCode = row.getCustomerId();
        if (customerCode == null || customerCode.trim().isEmpty()) {
            customerCode = "CUST_" + row.getMobileNo();
        }

        final String finalCustomerCode = customerCode;
        return customerRepository.findByCustomerCode(finalCustomerCode)
                .orElseGet(() -> {
                    // Customer not found, create a new one
                    Customer newCustomer = Customer.builder()
                            .customerCode(finalCustomerCode)
                            .customerId(row.getCustomerId())
                            .fullName(row.getCustomerName())
                            .mobileNumber(row.getMobileNo())
                            .secondaryMobileNumber(row.getSecondaryMobileNumber())
                            .resiPhone(row.getResiPhone())
                            .additionalPhone2(row.getAdditionalPhone2())
                            .email(row.getEmail())
                            .primaryAddress(row.getPrimaryAddress())
                            .secondaryAddress(row.getSecondaryAddress())
                            .city(row.getCity())
                            .state(row.getState())
                            .pincode(row.getPincode())
                            .fatherSpouseName(row.getFatherSpouseName())
                            .employerOrBusinessEntity(row.getEmployerOrBusinessEntity())
                            .reference1Name(row.getReference1Name())
                            .reference1Number(row.getReference1Number())
                            .reference2Name(row.getReference2Name())
                            .reference2Number(row.getReference2Number())
                            .languagePreference(row.getLanguage() != null ? row.getLanguage().toLowerCase() : "en")
                            .customerType("INDIVIDUAL")
                            .isActive(true)
                            .build();
                    return customerRepository.save(newCustomer);
                });
    }

    @SuppressWarnings("null")
    private LoanDetails createLoanDetails(CaseCsvRowDTO row, Customer customer) {
        return loanDetailsRepository.save(LoanDetails.builder()
                // Account identification
                .loanAccountNumber(row.getAccountNo())
                .lender(row.getLender())
                .coLender(row.getCoLender())
                .referenceLender(row.getReferenceLender())
                .productType(row.getProduct())
                .schemeCode(row.getSchemeCode())
                .productSourcingType(row.getProductSourcingType())
                // Customer
                .primaryCustomer(customer)
                // Amounts
                .loanAmount(parseBigDecimal(row.getLoanAmountOrLimit()))
                .totalOutstanding(parseBigDecimal(row.getOverdueAmount()))
                .pos(parseBigDecimal(row.getPos()))
                .tos(parseBigDecimal(row.getTos()))
                .emiAmount(parseBigDecimal(row.getEmiAmount()))
                .penaltyAmount(parseBigDecimal(row.getPenaltyAmount()))
                .charges(parseBigDecimal(row.getCharges()))
                .odInterest(parseBigDecimal(row.getOdInterest()))
                // Overdue breakdown
                .principalOverdue(parseBigDecimal(row.getPrincipalOverdue()))
                .interestOverdue(parseBigDecimal(row.getInterestOverdue()))
                .feesOverdue(parseBigDecimal(row.getFeesOverdue()))
                .penaltyOverdue(parseBigDecimal(row.getPenaltyOverdue()))
                // EMI details
                .emiStartDate(parseDate(row.getEmiStartDate()))
                .noOfPaidEmi(parseInteger(row.getNoOfPaidEmi()))
                .noOfPendingEmi(parseInteger(row.getNoOfPendingEmi()))
                .emiOverdueFrom(parseDate(row.getEmiOverdueFrom()))
                .nextEmiDate(parseDate(row.getNextEmiDate()))
                // DPD & Bucket
                .dpd(parseInteger(row.getDpd()))
                .riskBucket(row.getRiskBucket())
                .somBucket(row.getSomBucket())
                .somDpd(parseInteger(row.getSomDpd()))
                .cycleDue(row.getCycleDue())
                // Rates
                .roi(parseBigDecimal(row.getRoi()))
                .loanDuration(row.getLoanDuration())
                // Dates
                .loanDisbursementDate(parseDate(row.getDateOfDisbursement()))
                .loanMaturityDate(parseDate(row.getMaturityDate()))
                .dueDate(parseDate(row.getDueDate()))
                .writeoffDate(parseDate(row.getWriteoffDate()))
                // Credit card
                .minimumAmountDue(parseBigDecimal(row.getMinimumAmountDue()))
                .cardOutstanding(parseBigDecimal(row.getCardOutstanding()))
                .statementDate(parseDate(row.getStatementDate()))
                .statementMonth(row.getStatementMonth())
                .cardStatus(row.getCardStatus())
                .lastBilledAmount(parseBigDecimal(row.getLastBilledAmount()))
                .last4Digits(row.getLast4Digits())
                // Payment info
                .lastPaymentDate(parseDate(row.getLastPaymentDate()))
                .lastPaymentMode(row.getLastPaymentMode())
                .lastPaidAmount(parseBigDecimal(row.getLastPaidAmount()))
                // Bank details
                .beneficiaryAccountNumber(row.getBeneficiaryAccountNumber())
                .beneficiaryAccountName(row.getBeneficiaryAccountName())
                .repaymentBankName(row.getRepaymentBankName())
                .repaymentIfscCode(row.getRepaymentIfscCode())
                .referenceUrl(row.getReferenceUrl())
                // Block status
                .block1(row.getBlock1())
                .block1Date(parseDate(row.getBlock1Date()))
                .block2(row.getBlock2())
                .block2Date(parseDate(row.getBlock2Date()))
                // Sourcing
                .sourcingRmName(row.getSourcingRmName())
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

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", value);
            return null;
        }
    }

    /**
     * Log batch error in a new transaction
     * Stores original row data as JSON for export
     */
    @SuppressWarnings("null")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBatchErrorInNewTransaction(String batchId, CaseCsvRowDTO row, String errorMessage) {
        ErrorType errorType = determineErrorType(errorMessage);

        // Convert row to JSON for storing original data
        String originalRowData = convertRowToJson(row);

        batchErrorRepository.save(BatchError.builder()
                .batchId(batchId)
                .rowNumber(row.getRowNumber())
                .externalCaseId(row.getAccountNo()) // Use ACCOUNT NO as external case ID
                .errorType(errorType)
                .errorMessage(errorMessage)
                .originalRowData(originalRowData)
                .build());
    }

    /**
     * Convert CaseCsvRowDTO to JSON string for storage
     */
    private String convertRowToJson(CaseCsvRowDTO row) {
        try {
            return objectMapper.writeValueAsString(row);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert row to JSON: {}", e.getMessage());
            return null;
        }
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

    private String generateCaseNumber() {
        // Format: CASE-YYYY-NNNNNN
        String year = String.valueOf(LocalDate.now().getYear());
        long count = caseRepository.countTotalCases();
        return String.format("CASE-%s-%06d", year, count + 1);
    }
}

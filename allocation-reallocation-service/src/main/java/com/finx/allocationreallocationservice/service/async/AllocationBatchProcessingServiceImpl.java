package com.finx.allocationreallocationservice.service.async;

import com.finx.allocationreallocationservice.domain.dto.AllocationCsvRow;
import com.finx.allocationreallocationservice.domain.dto.ContactUpdateCsvRow;
import com.finx.allocationreallocationservice.domain.dto.ReallocationCsvRow;
import com.finx.allocationreallocationservice.domain.entity.AllocationBatch;
import com.finx.allocationreallocationservice.domain.entity.AllocationHistory;
import com.finx.allocationreallocationservice.domain.entity.BatchError;
import com.finx.allocationreallocationservice.domain.entity.Case;
import com.finx.allocationreallocationservice.domain.entity.CaseAllocation;
import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import com.finx.allocationreallocationservice.domain.enums.AllocationStatus;
import com.finx.allocationreallocationservice.domain.enums.BatchStatus;
import com.finx.allocationreallocationservice.domain.enums.ErrorType;
import com.finx.allocationreallocationservice.repository.AllocationBatchRepository;
import com.finx.allocationreallocationservice.repository.AllocationHistoryRepository;
import com.finx.allocationreallocationservice.repository.BatchErrorRepository;
import com.finx.allocationreallocationservice.repository.CaseAllocationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.finx.allocationreallocationservice.domain.entity.ContactUpdateBatch;
import com.finx.allocationreallocationservice.repository.ContactUpdateBatchRepository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationBatchProcessingServiceImpl implements AllocationBatchProcessingService {

    private final AllocationBatchRepository allocationBatchRepository;
    private final ContactUpdateBatchRepository contactUpdateBatchRepository;
    private final BatchErrorRepository batchErrorRepository;
    private final CaseAllocationRepository caseAllocationRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final com.finx.allocationreallocationservice.repository.UserRepository userRepository;
    private final com.finx.allocationreallocationservice.repository.CaseReadRepository caseReadRepository;
    private final com.finx.allocationreallocationservice.repository.CustomerRepository customerRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    @Override
    @Async("batchProcessingExecutor")
    @Transactional
    public void processAllocationBatchAsync(String batchId, String filePath) {
        log.info("Starting async processing for allocation batch: {}", batchId);
        Path path = Paths.get(filePath);

        AllocationBatch batch = allocationBatchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));

        List<BatchError> errors = new ArrayList<>();
        List<CaseAllocation> allocations = new ArrayList<>();
        List<AllocationHistory> historyEntries = new ArrayList<>();
        java.util.Map<Long, Integer> agentCaseCount = new java.util.HashMap<>(); // Track cases allocated to each agent

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            // Configure CSV parser to properly handle quoted fields with commas
            var csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withIgnoreQuotations(false)
                    .withStrictQuotes(false)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .build();

            CsvToBean<AllocationCsvRow> csvToBean = new CsvToBeanBuilder<AllocationCsvRow>(csvReader)
                    .withType(AllocationCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build();

            List<AllocationCsvRow> rows = csvToBean.parse();

            // Log any captured exceptions for debugging
            var exceptions = csvToBean.getCapturedExceptions();
            if (!exceptions.isEmpty()) {
                log.warn("Allocation CSV parsing completed with {} warnings/errors", exceptions.size());
                exceptions.forEach(ex -> log.warn("CSV parsing issue at line {}: {}",
                        ex.getLineNumber(), ex.getMessage()));
            }
            batch.setTotalCases(rows.size());

            AtomicInteger successfulAllocations = new AtomicInteger(0);
            AtomicInteger failedAllocations = new AtomicInteger(0);
            final int[] rowNumber = { 1 };

            rows.forEach(row -> {
                String validationError = getValidationError(row);
                if (validationError == null) {
                    // Lookup case_id by loan_id (user-friendly identifier)
                    Case caseEntity = caseReadRepository.findByLoanId(row.getLoanId())
                            .orElseThrow(() -> new RuntimeException("Case not found for loan_id: " + row.getLoanId()));
                    Long caseId = caseEntity.getId();
                    Long primaryAgentId = Long.parseLong(row.getPrimaryAgentId());

                    allocations.add(CaseAllocation.builder()
                            .caseId(caseId)
                            .externalCaseId(caseEntity.getExternalCaseId())
                            .primaryAgentId(primaryAgentId)
                            .secondaryAgentId(row.getSecondaryAgentId() != null && !row.getSecondaryAgentId().isEmpty()
                                    ? Long.parseLong(row.getSecondaryAgentId())
                                    : null)
                            .allocatedToType("USER")
                            .allocationType(row.getAllocationType() != null && !row.getAllocationType().isEmpty()
                                    ? row.getAllocationType().toUpperCase()
                                    : "PRIMARY")
                            .workloadPercentage(
                                    row.getAllocationPercentage() != null && !row.getAllocationPercentage().isEmpty()
                                            ? new java.math.BigDecimal(row.getAllocationPercentage())
                                            : null)
                            .geographyCode(row.getGeography() != null && !row.getGeography().isEmpty()
                                    ? row.getGeography().toUpperCase()
                                    : null)
                            .status(AllocationStatus.ALLOCATED)
                            .batchId(batchId)
                            .allocatedAt(LocalDateTime.now())
                            .build());

                    historyEntries.add(AllocationHistory.builder()
                            .caseId(caseId)
                            .allocatedToUserId(primaryAgentId)
                            .newOwnerType("USER")
                            .previousOwnerType("USER")
                            .allocatedAt(LocalDateTime.now())
                            .action(AllocationAction.ALLOCATED)
                            .reason(row.getRemarks() != null && !row.getRemarks().isEmpty() ? row.getRemarks()
                                    : "Batch allocation: " + batchId)
                            .batchId(batchId)
                            .build());

                    // Track agent case count for statistics update
                    agentCaseCount.put(primaryAgentId, agentCaseCount.getOrDefault(primaryAgentId, 0) + 1);

                    successfulAllocations.incrementAndGet();
                } else {
                    log.error("Validation failed for row {}: {}", rowNumber[0], validationError);
                    errors.add(buildErrorWithData(batchId, rowNumber[0], validationError, row));
                    failedAllocations.incrementAndGet();
                }
                rowNumber[0]++;
            });

            batch.setSuccessfulAllocations(successfulAllocations.get());
            batch.setFailedAllocations(failedAllocations.get());
            batch.setStatus(BatchStatus.COMPLETED);
            batch.setCompletedAt(LocalDateTime.now());

            allocationBatchRepository.save(batch);
            batchErrorRepository.saveAll(errors);
            caseAllocationRepository.saveAll(allocations);
            allocationHistoryRepository.saveAll(historyEntries);

            // CRITICAL: Update cases table to reflect allocation
            updateCasesTableForAllocation(allocations);

            // Update user statistics for allocated agents
            updateUserStatisticsForAllocation(agentCaseCount);

            log.info("Finished processing allocation batch: {}. Total: {}, Success: {}, Failed: {}",
                    batchId, rows.size(), successfulAllocations.get(), failedAllocations.get());

        } catch (Exception e) {
            log.error("Fatal error processing allocation batch {}: {}", batchId, e.getMessage(), e);
            batch.setStatus(BatchStatus.FAILED);
            batch.setCompletedAt(LocalDateTime.now());
            allocationBatchRepository.save(batch);
        } finally {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("Failed to delete temporary file: {}", filePath, e);
            }
        }
    }

    private String getValidationError(AllocationCsvRow row) {
        // Validate loan_id is provided
        if (row.getLoanId() == null || row.getLoanId().trim().isEmpty()) {
            return "loan_id is required";
        }

        // CRITICAL: Validate case exists in cases table by loan_id
        Optional<Case> caseOpt = caseReadRepository.findByLoanId(row.getLoanId());
        if (!caseOpt.isPresent()) {
            return "Case not found for loan_id: " + row.getLoanId() +
                   ". Please ensure case with this loan ID exists in cases table before allocation.";
        }

        // Validate primary_agent_id format
        Long primaryAgentId;
        try {
            primaryAgentId = Long.parseLong(row.getPrimaryAgentId());
        } catch (NumberFormatException e) {
            return "Invalid primary_agent_id: " + row.getPrimaryAgentId();
        }

        // Validate primary_agent_id exists in users table
        if (!userRepository.existsById(primaryAgentId)) {
            return "User not found for primary_agent_id: " + row.getPrimaryAgentId();
        }

        // Validate secondary_agent_id format and existence (if provided)
        if (row.getSecondaryAgentId() != null && !row.getSecondaryAgentId().isEmpty()) {
            Long secondaryAgentId;
            try {
                secondaryAgentId = Long.parseLong(row.getSecondaryAgentId());
            } catch (NumberFormatException e) {
                return "Invalid secondary_agent_id: " + row.getSecondaryAgentId();
            }

            // Validate secondary_agent_id exists in users table
            if (!userRepository.existsById(secondaryAgentId)) {
                return "User not found for secondary_agent_id: " + row.getSecondaryAgentId();
            }
        }

        // Validate allocation_percentage format (if provided)
        if (row.getAllocationPercentage() != null && !row.getAllocationPercentage().isEmpty()) {
            try {
                Double.parseDouble(row.getAllocationPercentage());
            } catch (NumberFormatException e) {
                return "Invalid allocation_percentage: " + row.getAllocationPercentage();
            }
        }

        return null;
    }

    @Override
    @Async("batchProcessingExecutor")
    @Transactional
    public void processReallocationBatchAsync(String batchId, String filePath) {
        log.info("Starting async processing for reallocation batch: {}", batchId);
        Path path = Paths.get(filePath);

        AllocationBatch batch = allocationBatchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));

        List<BatchError> errors = new ArrayList<>();
        List<CaseAllocation> allocationsToUpdate = new ArrayList<>();
        List<AllocationHistory> historyToSave = new ArrayList<>();
        java.util.Map<Long, Integer> agentDecrements = new java.util.HashMap<>(); // Track cases removed from agents
        java.util.Map<Long, Integer> agentIncrements = new java.util.HashMap<>(); // Track cases added to agents

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            // Configure CSV parser to properly handle quoted fields with commas
            var csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withIgnoreQuotations(false)
                    .withStrictQuotes(false)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .build();

            CsvToBean<ReallocationCsvRow> csvToBean = new CsvToBeanBuilder<ReallocationCsvRow>(csvReader)
                    .withType(ReallocationCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build();

            List<ReallocationCsvRow> rows = csvToBean.parse();

            // Log any captured exceptions for debugging
            var exceptions = csvToBean.getCapturedExceptions();
            if (!exceptions.isEmpty()) {
                log.warn("Reallocation CSV parsing completed with {} warnings/errors", exceptions.size());
                exceptions.forEach(ex -> log.warn("CSV parsing issue at line {}: {}",
                        ex.getLineNumber(), ex.getMessage()));
            }
            batch.setTotalCases(rows.size());

            AtomicInteger successfulAllocations = new AtomicInteger(0);
            AtomicInteger failedAllocations = new AtomicInteger(0);
            final int[] rowNumber = { 1 };

            for (ReallocationCsvRow row : rows) {
                String validationError = getValidationError(row);
                if (validationError == null) {
                    Long caseId = Long.parseLong(row.getCaseId());
                    Long currentAgentId = Long.parseLong(row.getCurrentAgentId());
                    Long newAgentId = Long.parseLong(row.getNewAgentId());

                    Optional<CaseAllocation> allocationOpt = caseAllocationRepository
                            .findFirstByCaseIdOrderByAllocatedAtDesc(caseId);
                    if (allocationOpt.isPresent()) {
                        CaseAllocation allocation = allocationOpt.get();
                        if (allocation.getPrimaryAgentId().equals(currentAgentId)) {
                            // Fetch case entity to get geography code
                            String geographyCode = allocation.getGeographyCode(); // Keep existing if available
                            try {
                                Optional<com.finx.allocationreallocationservice.domain.entity.Case> caseOpt = caseReadRepository
                                        .findById(caseId);
                                if (caseOpt.isPresent()) {
                                    geographyCode = caseOpt.get().getGeographyCode();
                                }
                            } catch (Exception e) {
                                log.warn("Failed to fetch case entity for caseId {}: {}", caseId, e.getMessage());
                            }

                            // Update allocation
                            allocation.setPrimaryAgentId(newAgentId);
                            // Maintain workload_percentage (default 100.00 if not set)
                            if (allocation.getWorkloadPercentage() == null) {
                                allocation.setWorkloadPercentage(new java.math.BigDecimal("100.00"));
                            }
                            // Update geography code
                            if (geographyCode != null) {
                                allocation.setGeographyCode(geographyCode);
                            }
                            allocationsToUpdate.add(allocation);

                            // Track agent statistics changes
                            agentDecrements.put(currentAgentId, agentDecrements.getOrDefault(currentAgentId, 0) + 1);
                            agentIncrements.put(newAgentId, agentIncrements.getOrDefault(newAgentId, 0) + 1);

                            historyToSave.add(AllocationHistory.builder()
                                    .caseId(allocation.getCaseId())
                                    .allocatedToUserId(newAgentId)
                                    .allocatedFromUserId(currentAgentId)
                                    .newOwnerType("USER")
                                    .previousOwnerType("USER")
                                    .allocatedAt(LocalDateTime.now())
                                    .action(AllocationAction.REALLOCATED)
                                    .reason(row.getReallocationReason())
                                    .batchId(batchId)
                                    .build());
                            successfulAllocations.incrementAndGet();
                        } else {
                            errors.add(buildErrorWithData(batchId, rowNumber[0], "Case not allocated to current_agent_id", row));
                            failedAllocations.incrementAndGet();
                        }
                    } else {
                        errors.add(buildErrorWithData(batchId, rowNumber[0], "Case allocation not found", row));
                        failedAllocations.incrementAndGet();
                    }
                } else {
                    errors.add(buildErrorWithData(batchId, rowNumber[0], validationError, row));
                    failedAllocations.incrementAndGet();
                }
                rowNumber[0]++;
            }

            batch.setSuccessfulAllocations(successfulAllocations.get());
            batch.setFailedAllocations(failedAllocations.get());
            batch.setStatus(BatchStatus.COMPLETED);
            batch.setCompletedAt(LocalDateTime.now());

            allocationBatchRepository.save(batch);
            batchErrorRepository.saveAll(errors);
            caseAllocationRepository.saveAll(allocationsToUpdate);
            allocationHistoryRepository.saveAll(historyToSave);

            // CRITICAL: Update cases table to reflect reallocation
            updateCasesTableForReallocation(allocationsToUpdate);

            // Update user statistics for both old and new agents
            updateUserStatisticsForReallocation(agentDecrements, agentIncrements);

            log.info("Finished processing reallocation batch: {}. Total: {}, Success: {}, Failed: {}",
                    batchId, rows.size(), successfulAllocations.get(), failedAllocations.get());

        } catch (Exception e) {
            log.error("Fatal error processing reallocation batch {}: {}", batchId, e.getMessage(), e);
            batch.setStatus(BatchStatus.FAILED);
            batch.setCompletedAt(LocalDateTime.now());
            allocationBatchRepository.save(batch);
        } finally {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("Failed to delete temporary file: {}", filePath, e);
            }
        }
    }

    private String getValidationError(ReallocationCsvRow row) {
        // Validate case_id format
        try {
            Long.parseLong(row.getCaseId());
        } catch (NumberFormatException e) {
            return "Invalid case_id: " + row.getCaseId();
        }

        // Validate current_agent_id format
        Long currentAgentId;
        try {
            currentAgentId = Long.parseLong(row.getCurrentAgentId());
        } catch (NumberFormatException e) {
            return "Invalid current_agent_id: " + row.getCurrentAgentId();
        }

        // Validate current_agent_id exists in users table
        if (!userRepository.existsById(currentAgentId)) {
            return "User not found for current_agent_id: " + row.getCurrentAgentId();
        }

        // Validate new_agent_id format
        Long newAgentId;
        try {
            newAgentId = Long.parseLong(row.getNewAgentId());
        } catch (NumberFormatException e) {
            return "Invalid new_agent_id: " + row.getNewAgentId();
        }

        // Validate new_agent_id exists in users table
        if (!userRepository.existsById(newAgentId)) {
            return "User not found for new_agent_id: " + row.getNewAgentId();
        }

        // Validate effective_date format (if provided)
        if (row.getEffectiveDate() != null && !row.getEffectiveDate().isEmpty()) {
            try {
                LocalDate.parse(row.getEffectiveDate());
            } catch (DateTimeParseException e) {
                return "Invalid effective_date format: " + row.getEffectiveDate();
            }
        }

        return null;
    }

    @SuppressWarnings("null")
    @Override
    @Async("batchProcessingExecutor")
    @Transactional
    public void processContactUpdateBatchAsync(String batchId, String filePath) {
        log.info("Starting async processing for contact update batch: {}", batchId);
        Path path = Paths.get(filePath);

        ContactUpdateBatch batch = contactUpdateBatchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Contact update batch not found: " + batchId));

        List<BatchError> errors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            // Configure CSV parser to properly handle quoted fields with commas
            var csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withIgnoreQuotations(false)
                    .withStrictQuotes(false)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .build();

            CsvToBean<ContactUpdateCsvRow> csvToBean = new CsvToBeanBuilder<ContactUpdateCsvRow>(csvReader)
                    .withType(ContactUpdateCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build();

            List<ContactUpdateCsvRow> rows = csvToBean.parse();

            // Log any captured exceptions for debugging
            var exceptions = csvToBean.getCapturedExceptions();
            if (!exceptions.isEmpty()) {
                log.warn("Contact update CSV parsing completed with {} warnings/errors", exceptions.size());
                exceptions.forEach(ex -> log.warn("CSV parsing issue at line {}: {}",
                        ex.getLineNumber(), ex.getMessage()));
            }
            batch.setTotalRecords(rows.size());

            AtomicInteger successfulUpdates = new AtomicInteger(0);
            AtomicInteger failedUpdates = new AtomicInteger(0);
            final int[] rowNumber = { 1 };

            for (ContactUpdateCsvRow row : rows) {
                String validationError = getValidationError(row);
                if (validationError == null) {
                    try {
                        String loanId = row.getLoanId();

                        // 1. Find the Case entity by loan account number
                        com.finx.allocationreallocationservice.domain.entity.Case caseEntity = caseReadRepository
                                .findByLoanId(loanId)
                                .orElseThrow(() -> new RuntimeException("Case not found for loan_id: " + loanId));

                        // 2. Get the primary customer from the LoanDetails associated with the case
                        // Assuming contact updates are always for the primary customer of the loan
                        Long customerId = caseEntity.getLoan().getPrimaryCustomer().getId();
                        com.finx.allocationreallocationservice.domain.entity.Customer customer = customerRepository
                                .findById(customerId)
                                .orElseThrow(() -> new RuntimeException("Customer not found for ID: " + customerId));

                        // 3. Update customer contact information based on updateType
                        String updateType = row.getUpdateType().toUpperCase();
                        boolean updated = false;

                        switch (updateType) {
                            case "MOBILE_UPDATE":
                                if (row.getMobileNumber() != null && !row.getMobileNumber().trim().isEmpty()) {
                                    customer.setMobileNumber(row.getMobileNumber());
                                    updated = true;
                                }
                                if (row.getAlternateMobile() != null && !row.getAlternateMobile().trim().isEmpty()) {
                                    customer.setAlternateMobile(row.getAlternateMobile());
                                    updated = true;
                                }
                                break;
                            case "EMAIL_UPDATE":
                                if (row.getEmail() != null && !row.getEmail().trim().isEmpty()) {
                                    customer.setEmailAddress(row.getEmail());
                                    updated = true;
                                }
                                if (row.getAlternateEmail() != null && !row.getAlternateEmail().trim().isEmpty()) {
                                    customer.setAlternateEmail(row.getAlternateEmail());
                                    updated = true;
                                }
                                break;
                            case "ADDRESS_UPDATE":
                                if (row.getAddress() != null && !row.getAddress().trim().isEmpty()) {
                                    customer.setAddressLine1(row.getAddress());
                                    updated = true;
                                }
                                if (row.getCity() != null && !row.getCity().trim().isEmpty()) {
                                    customer.setCity(row.getCity());
                                    updated = true;
                                }
                                if (row.getState() != null && !row.getState().trim().isEmpty()) {
                                    customer.setState(row.getState());
                                    updated = true;
                                }
                                if (row.getPincode() != null && !row.getPincode().trim().isEmpty()) {
                                    customer.setPincode(row.getPincode());
                                    updated = true;
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid update_type: " + row.getUpdateType());
                        }

                        if (updated) {
                            customerRepository.save(customer);
                            log.debug("Contact updated successfully for loan {}: {}", row.getLoanId(),
                                    row.getUpdateType());
                            successfulUpdates.incrementAndGet();
                        } else {
                            errors.add(buildErrorWithData(batchId, rowNumber[0],
                                    "No contact information provided for update type: " + row.getUpdateType(), row));
                            failedUpdates.incrementAndGet();
                        }

                    } catch (Exception e) {
                        log.error("Failed to update contact for loan {}: {}", row.getLoanId(), e.getMessage());
                        errors.add(buildErrorWithData(batchId, rowNumber[0], "Service error: " + e.getMessage(), row));
                        failedUpdates.incrementAndGet();
                    }
                } else {
                    errors.add(buildErrorWithData(batchId, rowNumber[0], validationError, row));
                    failedUpdates.incrementAndGet();
                }
                rowNumber[0]++;
            }

            batch.setSuccessfulUpdates(successfulUpdates.get());
            batch.setFailedUpdates(failedUpdates.get());
            batch.setStatus(BatchStatus.COMPLETED);
            batch.setCompletedAt(LocalDateTime.now());

            contactUpdateBatchRepository.save(batch);
            batchErrorRepository.saveAll(errors);

            log.info("Finished processing contact update batch: {}. Total: {}, Success: {}, Failed: {}",
                    batchId, rows.size(), successfulUpdates.get(), failedUpdates.get());

        } catch (Exception e) {
            log.error("Fatal error processing contact update batch {}: {}", batchId, e.getMessage(), e);
            batch.setStatus(BatchStatus.FAILED);
            batch.setCompletedAt(LocalDateTime.now());
            contactUpdateBatchRepository.save(batch);
        } finally {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("Failed to delete temporary file: {}", filePath, e);
            }
        }
    }

    private String getValidationError(ContactUpdateCsvRow row) {
        // Validate loan_id
        if (row.getLoanId() == null || row.getLoanId().trim().isEmpty()) {
            return "loan_id is required";
        }

        // Validate update_type
        if (row.getUpdateType() == null || row.getUpdateType().trim().isEmpty()) {
            return "update_type is required";
        }

        String updateType = row.getUpdateType().toUpperCase();

        // Validate based on update_type
        switch (updateType) {
            case "MOBILE_UPDATE":
                if ((row.getMobileNumber() == null || row.getMobileNumber().trim().isEmpty()) &&
                        (row.getAlternateMobile() == null || row.getAlternateMobile().trim().isEmpty())) {
                    return "Either mobile_number or alternate_mobile is required for MOBILE_UPDATE";
                }
                // Validate mobile number format (10 digits) if provided
                if (row.getMobileNumber() != null && !row.getMobileNumber().trim().isEmpty()
                        && !row.getMobileNumber().matches("^[0-9]{10}$")) {
                    return "Invalid mobile_number format (must be 10 digits): " + row.getMobileNumber();
                }
                // Validate alternate mobile if provided
                if (row.getAlternateMobile() != null && !row.getAlternateMobile().trim().isEmpty()
                        && !row.getAlternateMobile().matches("^[0-9]{10}$")) {
                    return "Invalid alternate_mobile format (must be 10 digits): " + row.getAlternateMobile();
                }
                break;

            case "EMAIL_UPDATE":
                if ((row.getEmail() == null || row.getEmail().trim().isEmpty()) &&
                        (row.getAlternateEmail() == null || row.getAlternateEmail().trim().isEmpty())) {
                    return "Either email or alternate_email is required for EMAIL_UPDATE";
                }
                // Validate email format if provided
                if (row.getEmail() != null && !row.getEmail().trim().isEmpty()
                        && !EMAIL_PATTERN.matcher(row.getEmail()).matches()) {
                    return "Invalid email format: " + row.getEmail();
                }
                // Validate alternate email if provided
                if (row.getAlternateEmail() != null && !row.getAlternateEmail().trim().isEmpty()
                        && !EMAIL_PATTERN.matcher(row.getAlternateEmail()).matches()) {
                    return "Invalid alternate_email format: " + row.getAlternateEmail();
                }
                break;

            case "ADDRESS_UPDATE":
                if ((row.getAddress() == null || row.getAddress().trim().isEmpty()) &&
                        (row.getCity() == null || row.getCity().trim().isEmpty()) &&
                        (row.getState() == null || row.getState().trim().isEmpty()) &&
                        (row.getPincode() == null || row.getPincode().trim().isEmpty())) {
                    return "At least one address field (address, city, state, pincode) is required for ADDRESS_UPDATE";
                }
                // Validate pincode format if provided
                if (row.getPincode() != null && !row.getPincode().trim().isEmpty()
                        && !row.getPincode().matches("^[0-9]{6}$")) {
                    return "Invalid pincode format (must be 6 digits): " + row.getPincode();
                }
                break;

            default:
                return "Invalid update_type: " + row.getUpdateType()
                        + ". Must be MOBILE_UPDATE, EMAIL_UPDATE, or ADDRESS_UPDATE";
        }

        return null;
    }

    /**
     * Update user statistics after allocation
     * Increases current_case_count for agents and recalculates
     * allocation_percentage
     * 
     * @param agentCaseCount Map of agentId to number of cases allocated
     */
    @SuppressWarnings("null")
    private void updateUserStatisticsForAllocation(java.util.Map<Long, Integer> agentCaseCount) {
        log.info("Updating user statistics for allocation: {} agents affected", agentCaseCount.size());

        for (java.util.Map.Entry<Long, Integer> entry : agentCaseCount.entrySet()) {
            Long agentId = entry.getKey();
            Integer casesAllocated = entry.getValue();

            try {
                com.finx.allocationreallocationservice.domain.entity.User user = userRepository.findById(agentId)
                        .orElse(null);
                if (user == null) {
                    log.warn("User {} not found for statistics update", agentId);
                    continue;
                }

                // Increase current_case_count
                Integer currentCaseCount = user.getCurrentCaseCount() != null ? user.getCurrentCaseCount() : 0;
                Integer newCaseCount = currentCaseCount + casesAllocated;
                user.setCurrentCaseCount(newCaseCount);

                // Recalculate allocation_percentage: (current_case_count / max_case_capacity) *
                // 100
                Integer maxCapacity = user.getMaxCaseCapacity() != null ? user.getMaxCaseCapacity() : 100;
                if (maxCapacity > 0) {
                    double allocationPercentage = ((double) newCaseCount / maxCapacity) * 100.0;
                    // Round to 2 decimal places
                    allocationPercentage = Math.round(allocationPercentage * 100.0) / 100.0;
                    user.setAllocationPercentage(allocationPercentage);
                } else {
                    user.setAllocationPercentage(0.0);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info(
                        "Updated user {} statistics: allocated {} cases, currentCaseCount={}, allocationPercentage={}%",
                        agentId, casesAllocated, newCaseCount, user.getAllocationPercentage());

            } catch (Exception e) {
                log.error("Failed to update statistics for user {}: {}", agentId, e.getMessage(), e);
            }
        }
    }

    /**
     * Update user statistics after reallocation
     * Decreases current_case_count for old agents and increases for new agents
     * Recalculates allocation_percentage for all affected agents
     * 
     * @param agentDecrements Map of agentId to number of cases removed
     * @param agentIncrements Map of agentId to number of cases added
     */
    @SuppressWarnings("null")
    private void updateUserStatisticsForReallocation(java.util.Map<Long, Integer> agentDecrements,
            java.util.Map<Long, Integer> agentIncrements) {
        log.info("Updating user statistics for reallocation: {} agents decremented, {} agents incremented",
                agentDecrements.size(), agentIncrements.size());

        // Process decrements (cases removed from old agents)
        for (java.util.Map.Entry<Long, Integer> entry : agentDecrements.entrySet()) {
            Long agentId = entry.getKey();
            Integer casesRemoved = entry.getValue();

            try {
                com.finx.allocationreallocationservice.domain.entity.User user = userRepository.findById(agentId)
                        .orElse(null);
                if (user == null) {
                    log.warn("User {} not found for statistics update (decrement)", agentId);
                    continue;
                }

                // Decrease current_case_count
                Integer currentCaseCount = user.getCurrentCaseCount() != null ? user.getCurrentCaseCount() : 0;
                Integer newCaseCount = Math.max(0, currentCaseCount - casesRemoved); // Ensure non-negative
                user.setCurrentCaseCount(newCaseCount);

                // Recalculate allocation_percentage
                Integer maxCapacity = user.getMaxCaseCapacity() != null ? user.getMaxCaseCapacity() : 100;
                if (maxCapacity > 0) {
                    double allocationPercentage = ((double) newCaseCount / maxCapacity) * 100.0;
                    allocationPercentage = Math.round(allocationPercentage * 100.0) / 100.0;
                    user.setAllocationPercentage(allocationPercentage);
                } else {
                    user.setAllocationPercentage(0.0);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info(
                        "Decremented user {} statistics: removed {} cases, currentCaseCount={}, allocationPercentage={}%",
                        agentId, casesRemoved, newCaseCount, user.getAllocationPercentage());

            } catch (Exception e) {
                log.error("Failed to update statistics for user {} (decrement): {}", agentId, e.getMessage(), e);
            }
        }

        // Process increments (cases added to new agents)
        for (java.util.Map.Entry<Long, Integer> entry : agentIncrements.entrySet()) {
            Long agentId = entry.getKey();
            Integer casesAdded = entry.getValue();

            try {
                com.finx.allocationreallocationservice.domain.entity.User user = userRepository.findById(agentId)
                        .orElse(null);
                if (user == null) {
                    log.warn("User {} not found for statistics update (increment)", agentId);
                    continue;
                }

                // Increase current_case_count
                Integer currentCaseCount = user.getCurrentCaseCount() != null ? user.getCurrentCaseCount() : 0;
                Integer newCaseCount = currentCaseCount + casesAdded;
                user.setCurrentCaseCount(newCaseCount);

                // Recalculate allocation_percentage
                Integer maxCapacity = user.getMaxCaseCapacity() != null ? user.getMaxCaseCapacity() : 100;
                if (maxCapacity > 0) {
                    double allocationPercentage = ((double) newCaseCount / maxCapacity) * 100.0;
                    allocationPercentage = Math.round(allocationPercentage * 100.0) / 100.0;
                    user.setAllocationPercentage(allocationPercentage);
                } else {
                    user.setAllocationPercentage(0.0);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info(
                        "Incremented user {} statistics: added {} cases, currentCaseCount={}, allocationPercentage={}%",
                        agentId, casesAdded, newCaseCount, user.getAllocationPercentage());

            } catch (Exception e) {
                log.error("Failed to update statistics for user {} (increment): {}", agentId, e.getMessage(), e);
            }
        }
    }

    /**
     * CRITICAL FIX: Update cases table after allocation
     * Updates allocated_to_user_id, allocated_at, and case_status in cases table
     * This ensures cases are marked as ALLOCATED in the database
     *
     * @param allocations List of case allocations to apply to cases table
     */
    private void updateCasesTableForAllocation(List<CaseAllocation> allocations) {
        if (allocations.isEmpty()) {
            return;
        }

        log.info("Updating cases table for {} allocations", allocations.size());

        String updateSql = "UPDATE cases SET allocated_to_user_id = ?, allocated_at = ?, " +
                "case_status = 'ALLOCATED', updated_at = NOW() WHERE id = ?";

        int updatedCount = 0;
        for (CaseAllocation allocation : allocations) {
            try {
                int rowsAffected = jdbcTemplate.update(
                        updateSql,
                        allocation.getPrimaryAgentId(),
                        allocation.getAllocatedAt(),
                        allocation.getCaseId());

                if (rowsAffected > 0) {
                    updatedCount++;
                } else {
                    log.warn("Case {} not found in cases table for allocation update", allocation.getCaseId());
                }
            } catch (Exception e) {
                log.error("Failed to update cases table for case {}: {}", allocation.getCaseId(), e.getMessage());
            }
        }

        log.info("Successfully updated {} out of {} cases in cases table", updatedCount, allocations.size());
    }

    /**
     * CRITICAL FIX: Update cases table after reallocation
     * Updates allocated_to_user_id and allocated_at in cases table
     * Status remains ALLOCATED during reallocation
     *
     * @param allocations List of updated case allocations to apply to cases table
     */
    private void updateCasesTableForReallocation(List<CaseAllocation> allocations) {
        if (allocations.isEmpty()) {
            return;
        }

        log.info("Updating cases table for {} reallocations", allocations.size());

        String updateSql = "UPDATE cases SET allocated_to_user_id = ?, allocated_at = ?, " +
                "updated_at = NOW() WHERE id = ?";

        int updatedCount = 0;
        for (CaseAllocation allocation : allocations) {
            try {
                int rowsAffected = jdbcTemplate.update(
                        updateSql,
                        allocation.getPrimaryAgentId(),
                        allocation.getAllocatedAt(),
                        allocation.getCaseId());

                if (rowsAffected > 0) {
                    updatedCount++;
                } else {
                    log.warn("Case {} not found in cases table for reallocation update", allocation.getCaseId());
                }
            } catch (Exception e) {
                log.error("Failed to update cases table for case {}: {}", allocation.getCaseId(), e.getMessage());
            }
        }

        log.info("Successfully updated {} out of {} cases in cases table", updatedCount, allocations.size());
    }

    private BatchError buildError(String batchId, int rowNumber, String message, String caseId) {
        return BatchError.builder()
                .batchId(batchId)
                .rowNumber(rowNumber)
                .errorType(ErrorType.VALIDATION)
                .errorMessage(message)
                .externalCaseId(caseId)
                .build();
    }

    /**
     * Build error with original row data for allocation rows
     */
    private BatchError buildErrorWithData(String batchId, int rowNumber, String message, AllocationCsvRow row) {
        return BatchError.builder()
                .batchId(batchId)
                .rowNumber(rowNumber)
                .errorType(ErrorType.VALIDATION)
                .errorMessage(message)
                .externalCaseId(row.getCaseId())
                .originalRowData(convertToJson(row))
                .build();
    }

    /**
     * Build error with original row data for reallocation rows
     */
    private BatchError buildErrorWithData(String batchId, int rowNumber, String message, ReallocationCsvRow row) {
        return BatchError.builder()
                .batchId(batchId)
                .rowNumber(rowNumber)
                .errorType(ErrorType.VALIDATION)
                .errorMessage(message)
                .externalCaseId(row.getCaseId())
                .originalRowData(convertToJson(row))
                .build();
    }

    /**
     * Build error with original row data for contact update rows
     */
    private BatchError buildErrorWithData(String batchId, int rowNumber, String message, ContactUpdateCsvRow row) {
        return BatchError.builder()
                .batchId(batchId)
                .rowNumber(rowNumber)
                .errorType(ErrorType.VALIDATION)
                .errorMessage(message)
                .externalCaseId(row.getLoanId())
                .originalRowData(convertToJson(row))
                .build();
    }

    /**
     * Convert object to JSON string for storage
     */
    private String convertToJson(Object row) {
        try {
            return objectMapper.writeValueAsString(row);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert row to JSON: {}", e.getMessage());
            return null;
        }
    }
}

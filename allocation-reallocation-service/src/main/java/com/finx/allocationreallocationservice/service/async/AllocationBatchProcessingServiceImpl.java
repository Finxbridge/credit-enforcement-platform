package com.finx.allocationreallocationservice.service.async;

import com.finx.allocationreallocationservice.domain.dto.AllocationCsvRow;
import com.finx.allocationreallocationservice.domain.dto.ContactUpdateCsvRow;
import com.finx.allocationreallocationservice.domain.dto.ReallocationCsvRow;
import com.finx.allocationreallocationservice.domain.entity.AllocationBatch;
import com.finx.allocationreallocationservice.domain.entity.AllocationHistory;
import com.finx.allocationreallocationservice.domain.entity.BatchError;
import com.finx.allocationreallocationservice.domain.entity.CaseAllocation;
import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import com.finx.allocationreallocationservice.domain.enums.AllocationStatus;
import com.finx.allocationreallocationservice.domain.enums.BatchStatus;
import com.finx.allocationreallocationservice.domain.enums.ErrorType;
import com.finx.allocationreallocationservice.repository.AllocationBatchRepository;
import com.finx.allocationreallocationservice.repository.AllocationHistoryRepository;
import com.finx.allocationreallocationservice.repository.BatchErrorRepository;
import com.finx.allocationreallocationservice.repository.CaseAllocationRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
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

import com.finx.allocationreallocationservice.client.MasterDataClient;
import com.finx.allocationreallocationservice.client.dto.ContactUpdateRequestDTO;
import com.finx.allocationreallocationservice.client.dto.ContactUpdateResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationBatchProcessingServiceImpl implements AllocationBatchProcessingService {

    private final AllocationBatchRepository allocationBatchRepository;
    private final ContactUpdateBatchRepository contactUpdateBatchRepository;
    private final BatchErrorRepository batchErrorRepository;
    private final CaseAllocationRepository caseAllocationRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final MasterDataClient masterDataClient;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

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

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            CsvToBean<AllocationCsvRow> csvToBean = new CsvToBeanBuilder<AllocationCsvRow>(reader)
                    .withType(AllocationCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<AllocationCsvRow> rows = csvToBean.parse();
            batch.setTotalCases(rows.size());

            AtomicInteger successfulAllocations = new AtomicInteger(0);
            AtomicInteger failedAllocations = new AtomicInteger(0);
            final int[] rowNumber = {1};

            rows.forEach(row -> {
                String validationError = getValidationError(row);
                if (validationError == null) {
                    Long caseId = Long.parseLong(row.getCaseId());
                    Long primaryAgentId = Long.parseLong(row.getPrimaryAgentId());

                    allocations.add(CaseAllocation.builder()
                            .caseId(caseId)
                            .primaryAgentId(primaryAgentId)
                            .secondaryAgentId(row.getSecondaryAgentId() != null && !row.getSecondaryAgentId().isEmpty() ? Long.parseLong(row.getSecondaryAgentId()) : null)
                            .allocatedToType("USER")
                            .allocationType("PRIMARY")
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
                            .reason("Batch allocation: " + batchId)
                            .batchId(batchId)
                            .build());

                    successfulAllocations.incrementAndGet();
                } else {
                    log.error("Validation failed for row {}: {}", rowNumber[0], validationError);
                    errors.add(BatchError.builder()
                            .batchId(batchId)
                            .rowNumber(rowNumber[0])
                            .errorType(ErrorType.VALIDATION)
                            .errorMessage(validationError)
                            .externalCaseId(row.getCaseId())
                            .build());
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
        try {
            Long.parseLong(row.getCaseId());
        } catch (NumberFormatException e) {
            return "Invalid case_id: " + row.getCaseId();
        }
        try {
            Long.parseLong(row.getPrimaryAgentId());
        } catch (NumberFormatException e) {
            return "Invalid primary_agent_id: " + row.getPrimaryAgentId();
        }
        if (row.getSecondaryAgentId() != null && !row.getSecondaryAgentId().isEmpty()) {
            try {
                Long.parseLong(row.getSecondaryAgentId());
            } catch (NumberFormatException e) {
                return "Invalid secondary_agent_id: " + row.getSecondaryAgentId();
            }
        }
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

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            CsvToBean<ReallocationCsvRow> csvToBean = new CsvToBeanBuilder<ReallocationCsvRow>(reader)
                    .withType(ReallocationCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ReallocationCsvRow> rows = csvToBean.parse();
            batch.setTotalCases(rows.size());

            AtomicInteger successfulAllocations = new AtomicInteger(0);
            AtomicInteger failedAllocations = new AtomicInteger(0);
            final int[] rowNumber = {1};

            for (ReallocationCsvRow row : rows) {
                String validationError = getValidationError(row);
                if (validationError == null) {
                    Optional<CaseAllocation> allocationOpt = caseAllocationRepository.findFirstByCaseIdOrderByAllocatedAtDesc(Long.parseLong(row.getCaseId()));
                    if (allocationOpt.isPresent()) {
                        CaseAllocation allocation = allocationOpt.get();
                        if (allocation.getPrimaryAgentId().equals(Long.parseLong(row.getCurrentAgentId()))) {
                            allocation.setPrimaryAgentId(Long.parseLong(row.getNewAgentId()));
                            allocationsToUpdate.add(allocation);

                            historyToSave.add(AllocationHistory.builder()
                                    .caseId(allocation.getCaseId())
                                    .allocatedToUserId(allocation.getPrimaryAgentId())
                                    .allocatedFromUserId(Long.parseLong(row.getCurrentAgentId()))
                                    .newOwnerType("USER")
                                    .previousOwnerType("USER")
                                    .allocatedAt(LocalDateTime.now())
                                    .action(AllocationAction.REALLOCATED)
                                    .reason(row.getReallocationReason())
                                    .batchId(batchId)
                                    .build());
                            successfulAllocations.incrementAndGet();
                        } else {
                            errors.add(buildError(batchId, rowNumber[0], "Case not allocated to fromAgentId", row.getCaseId()));
                            failedAllocations.incrementAndGet();
                        }
                    } else {
                        errors.add(buildError(batchId, rowNumber[0], "Case not found", row.getCaseId()));
                        failedAllocations.incrementAndGet();
                    }
                } else {
                    errors.add(buildError(batchId, rowNumber[0], validationError, row.getCaseId()));
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
        try {
            Long.parseLong(row.getCaseId());
        } catch (NumberFormatException e) {
            return "Invalid case_id: " + row.getCaseId();
        }
        try {
            Long.parseLong(row.getCurrentAgentId());
        } catch (NumberFormatException e) {
            return "Invalid current_agent_id: " + row.getCurrentAgentId();
        }
        try {
            Long.parseLong(row.getNewAgentId());
        } catch (NumberFormatException e) {
            return "Invalid new_agent_id: " + row.getNewAgentId();
        }
        if (row.getEffectiveDate() != null && !row.getEffectiveDate().isEmpty()) {
            try {
                LocalDate.parse(row.getEffectiveDate());
            } catch (DateTimeParseException e) {
                return "Invalid effective_date format: " + row.getEffectiveDate();
            }
        }
        return null;
    }

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
            CsvToBean<ContactUpdateCsvRow> csvToBean = new CsvToBeanBuilder<ContactUpdateCsvRow>(reader)
                    .withType(ContactUpdateCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ContactUpdateCsvRow> rows = csvToBean.parse();
            batch.setTotalRecords(rows.size());

            AtomicInteger successfulUpdates = new AtomicInteger(0);
            AtomicInteger failedUpdates = new AtomicInteger(0);
            final int[] rowNumber = {1};

            for (ContactUpdateCsvRow row : rows) {
                String validationError = getValidationError(row);
                if (validationError == null) {
                    try {
                        // Call Master Data Service to update borrower contact information
                        ContactUpdateRequestDTO contactRequest = ContactUpdateRequestDTO.builder()
                                .mobileNumber(row.getMobileNumber())
                                .alternateMobile(row.getAlternateMobile())
                                .email(row.getEmail())
                                .alternateEmail(row.getAlternateEmail())
                                .address(row.getAddress())
                                .city(row.getCity())
                                .state(row.getState())
                                .pincode(row.getPincode())
                                .updateType(row.getUpdateType())
                                .build();

                        ContactUpdateResponseDTO response = masterDataClient.updateBorrowerContactInfo(
                                Long.parseLong(row.getCaseId()), contactRequest);

                        if (response != null && response.getSuccess()) {
                            log.debug("Contact updated successfully for case {}: {}", row.getCaseId(), row.getUpdateType());
                            successfulUpdates.incrementAndGet();
                        } else {
                            String errorMsg = response != null ? response.getMessage() : "Unknown error";
                            errors.add(buildError(batchId, rowNumber[0], "Contact update failed: " + errorMsg, row.getCaseId()));
                            failedUpdates.incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.error("Failed to update contact for case {}: {}", row.getCaseId(), e.getMessage());
                        errors.add(buildError(batchId, rowNumber[0], "Service error: " + e.getMessage(), row.getCaseId()));
                        failedUpdates.incrementAndGet();
                    }
                } else {
                    errors.add(buildError(batchId, rowNumber[0], validationError, row.getCaseId()));
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
        try {
            Long.parseLong(row.getCaseId());
        } catch (NumberFormatException e) {
            return "Invalid case_id: " + row.getCaseId();
        }
        if ((row.getMobileNumber() == null || row.getMobileNumber().isEmpty()) &&
                (row.getEmail() == null || row.getEmail().isEmpty()) &&
                (row.getAlternateMobile() == null || row.getAlternateMobile().isEmpty()) &&
                (row.getAlternateEmail() == null || row.getAlternateEmail().isEmpty())) {
            return "At least one of mobile_number, alternate_mobile, email or alternate_email must be provided";
        }
        if (row.getEmail() != null && !row.getEmail().isEmpty() && !EMAIL_PATTERN.matcher(row.getEmail()).matches()) {
            return "Invalid email format: " + row.getEmail();
        }
        if (row.getAlternateEmail() != null && !row.getAlternateEmail().isEmpty() && !EMAIL_PATTERN.matcher(row.getAlternateEmail()).matches()) {
            return "Invalid alternate_email format: " + row.getAlternateEmail();
        }
        return null;
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
}

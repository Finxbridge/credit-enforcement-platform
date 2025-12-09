package com.finx.casesourcingservice.service.impl;

import com.finx.casesourcingservice.config.RestPage;
import com.finx.casesourcingservice.domain.dto.*;
import com.finx.casesourcingservice.domain.entity.CaseBatch;
import com.finx.casesourcingservice.domain.entity.Case;
import com.finx.casesourcingservice.domain.entity.BatchError;
import com.finx.casesourcingservice.domain.enums.BatchStatus;
import com.finx.casesourcingservice.domain.enums.SourceType;
import com.finx.casesourcingservice.exception.BusinessException;
import com.finx.casesourcingservice.repository.CaseRepository;
import com.finx.casesourcingservice.repository.CaseBatchRepository;
import com.finx.casesourcingservice.repository.BatchErrorRepository;
import com.finx.casesourcingservice.service.CaseSourcingService;
import com.finx.casesourcingservice.service.async.BatchProcessingService;
import com.finx.casesourcingservice.util.csv.CsvExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSourcingServiceImpl implements CaseSourcingService {

        private final CaseRepository caseRepository;
        private final CaseBatchRepository caseBatchRepository;
        private final BatchErrorRepository batchErrorRepository;
        private final BatchProcessingService batchProcessingService;
        private final CsvExporter csvExporter;

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboardSummary")
        public DashboardSummaryDTO getDashboardSummary() {
                log.info("Fetching dashboard summary");

                Long totalReceived = caseBatchRepository.getTotalReceived();
                Long validated = caseBatchRepository.getTotalValidated();
                Long failed = caseBatchRepository.getTotalFailed();
                Long unallocated = caseRepository.countByCaseStatus("UNALLOCATED");

                return DashboardSummaryDTO.builder()
                                .totalReceived(totalReceived != null ? totalReceived : 0L)
                                .validated(validated != null ? validated : 0L)
                                .failed(failed != null ? failed : 0L)
                                .unallocated(unallocated != null ? unallocated : 0L)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "sourceStats")
        public List<SourceStatsDTO> getSourceStats() {
                log.info("Fetching source-wise statistics");

                List<Object[]> stats = caseBatchRepository.getSourceStats();

                return stats.stream()
                                .map(row -> SourceStatsDTO.builder()
                                                .source(row[0].toString())
                                                .total(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                                                .successful(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                                                .failed(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "recentUploads", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
        public List<RecentUploadDTO> getRecentUploads(Pageable pageable) {
                log.info("Fetching recent uploads");

                Page<CaseBatch> batches = caseBatchRepository.findAllByOrderByCreatedAtDesc(pageable);

                return batches.getContent().stream()
                                .map(batch -> RecentUploadDTO.builder()
                                                .batchId(batch.getBatchId())
                                                .source(batch.getSourceType().name())
                                                .uploadedBy(batch.getUploadedBy())
                                                .uploadedAt(batch.getCreatedAt())
                                                .totalCases(batch.getTotalCases())
                                                .status(batch.getStatus().name())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        @CacheEvict(value = { "dashboardSummary", "sourceStats", "recentUploads", "batchStatus", "batchSummary",
                        "failedCaseRecords", "unallocatedCases", "unallocatedCaseDetails", "intakeReport",
                        "unallocatedCasesReport" }, allEntries = true)
        @SuppressWarnings("null")
        public BatchUploadResponseDTO uploadCases(MultipartFile file, String uploadedBy) {
                log.info("Uploading case data from file: {}", file.getOriginalFilename());

                // Generate batch ID
                String batchId = generateBatchId();

                // Save the MultipartFile to a temporary location
                Path tempFilePath;
                try {
                        tempFilePath = Files.createTempFile("case_upload_", ".csv");
                        file.transferTo(tempFilePath);
                } catch (IOException e) {
                        log.error("Failed to save uploaded file to temporary location", e);
                        throw new BusinessException("Failed to process file upload: " + e.getMessage());
                }

                // Quick count of CSV rows (excluding header)
                int estimatedRowCount = 0;
                try {
                        estimatedRowCount = countCsvRows(tempFilePath);
                } catch (Exception e) {
                        log.warn("Failed to count CSV rows: {}", e.getMessage());
                }

                // Create batch record
                CaseBatch batch = CaseBatch.builder()
                                .batchId(batchId)
                                .sourceType(SourceType.MANUAL)
                                .status(BatchStatus.PROCESSING)
                                .fileName(file.getOriginalFilename())
                                .uploadedBy(uploadedBy)
                                .totalCases(estimatedRowCount)
                                .validCases(0)
                                .invalidCases(0)
                                .duplicateCases(0)
                                .validationJobId("job_" + System.currentTimeMillis())
                                .build();

                caseBatchRepository.save(batch);

                // Process file asynchronously
                batchProcessingService.processBatchAsync(batchId, tempFilePath.toString());

                return BatchUploadResponseDTO.builder()
                                .batchId(batchId)
                                .totalCases(batch.getTotalCases())
                                .status(batch.getStatus().name())
                                .validationJobId(batch.getValidationJobId())
                                .build();
        }

        private int countCsvRows(Path filePath) throws IOException {
                try (java.io.BufferedReader reader = Files.newBufferedReader(filePath)) {
                        // Skip header line
                        reader.readLine();

                        // Count data rows (skip empty lines)
                        int count = 0;
                        String line;
                        while ((line = reader.readLine()) != null) {
                                if (!line.trim().isEmpty()) {
                                        count++;
                                }
                        }
                        return count;
                }
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "batchStatus", key = "#batchId")
        public BatchStatusDTO getBatchStatus(String batchId) {
                log.info("Fetching batch status for batchId: {}", batchId);

                CaseBatch batch = caseBatchRepository.findByBatchId(batchId)
                                .orElseThrow(() -> new BusinessException("Batch not found: " + batchId));

                List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

                List<BatchErrorDTO> errorDTOs = errors.stream()
                                .map(error -> BatchErrorDTO.builder()
                                                .externalCaseId(error.getExternalCaseId())
                                                .errorType(error.getErrorType().name())
                                                .message(error.getErrorMessage())
                                                .build())
                                .collect(Collectors.toList());

                return BatchStatusDTO.builder()
                                .batchId(batch.getBatchId())
                                .totalCases(batch.getTotalCases())
                                .validCases(batch.getValidCases())
                                .invalidCases(batch.getInvalidCases())
                                .status(batch.getStatus().name())
                                .errors(errorDTOs)
                                .completedAt(batch.getCompletedAt())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "batchSummary", key = "#batchId")
        public BatchSummaryDTO getBatchSummary(String batchId) {
                log.info("Fetching batch summary for batchId: {}", batchId);

                CaseBatch batch = caseBatchRepository.findByBatchId(batchId)
                                .orElseThrow(() -> new BusinessException("Batch not found: " + batchId));

                return BatchSummaryDTO.builder()
                                .batchId(batch.getBatchId())
                                .validCases(batch.getValidCases())
                                .invalidCases(batch.getInvalidCases())
                                .duplicates(batch.getDuplicateCases())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "failedCaseRecords", key = "#batchId")
        public List<FailedCaseRecordDTO> getFailedCaseRecords(String batchId) {
                log.info("Fetching failed case records for batchId: {}", batchId);

                List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

                // Group errors by row number and external case ID
                Map<String, FailedCaseRecordDTO> recordMap = new HashMap<>();

                for (BatchError error : errors) {
                        String key = error.getRowNumber() + "_" + error.getExternalCaseId();

                        recordMap.computeIfAbsent(key, k -> FailedCaseRecordDTO.builder()
                                        .rowNumber(error.getRowNumber())
                                        .externalCaseId(error.getExternalCaseId())
                                        .errors(new ArrayList<>())
                                        .build())
                                        .getErrors().add(error.getErrorMessage());
                }

                return new ArrayList<>(recordMap.values());
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "unallocatedCases", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
        public Page<UnallocatedCaseDTO> getUnallocatedCases(Pageable pageable) {
                log.info("Fetching unallocated ACTIVE cases");

                // Use findByCaseStatusAndActive to get only ACTIVE (status=200) unallocated cases
                Page<Case> cases = caseRepository.findByCaseStatusAndActive("UNALLOCATED", pageable);

                // Map to DTOs
                Page<UnallocatedCaseDTO> mappedPage = cases.map(this::mapToUnallocatedCaseDTO);

                // Wrap in RestPage for proper Redis serialization/deserialization
                return new RestPage<>(mappedPage.getContent(), pageable, mappedPage.getTotalElements());
        }

        @SuppressWarnings("null")
        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "unallocatedCaseDetails", key = "#caseId")
        public UnallocatedCaseDTO getUnallocatedCaseDetails(Long caseId) {
                log.info("Fetching unallocated case details for caseId: {}", caseId);

                Case caseEntity = caseRepository.findById(caseId)
                                .orElseThrow(() -> new BusinessException("Case not found: " + caseId));

                if (!"UNALLOCATED".equals(caseEntity.getCaseStatus())) {
                        throw new BusinessException("Case is not unallocated: " + caseId);
                }

                return mapToUnallocatedCaseDTO(caseEntity);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "dashboardSummary", "sourceStats", "recentUploads", "batchStatus", "batchSummary",
                        "failedCaseRecords", "unallocatedCases", "unallocatedCaseDetails", "intakeReport",
                        "unallocatedCasesReport" }, allEntries = true)
        @SuppressWarnings("null")
        public BatchUploadResponseDTO reuploadCases(String batchId, MultipartFile file, String uploadedBy) {
                log.info("Re-uploading corrected cases for batchId: {}", batchId);

                // Verify original batch exists
                if (!caseBatchRepository.existsByBatchId(batchId)) {
                        throw new BusinessException("Original batch not found: " + batchId);
                }

                // Generate new batch ID for reupload
                String newBatchId = batchId + "_REUPLOAD_" + System.currentTimeMillis();

                // Create new batch record
                CaseBatch batch = CaseBatch.builder()
                                .batchId(newBatchId)
                                .sourceType(SourceType.MANUAL)
                                .status(BatchStatus.PROCESSING)
                                .fileName(file.getOriginalFilename())
                                .uploadedBy(uploadedBy)
                                .totalCases(0)
                                .validCases(0)
                                .invalidCases(0)
                                .duplicateCases(0)
                                .validationJobId("job_" + System.currentTimeMillis())
                                .build();

                caseBatchRepository.save(batch);

                // Save the MultipartFile to a temporary location
                Path tempFilePath;
                try {
                        tempFilePath = Files.createTempFile("case_reupload_", ".csv");
                        file.transferTo(tempFilePath);
                } catch (IOException e) {
                        log.error("Failed to save re-uploaded file to temporary location", e);
                        throw new BusinessException("Failed to process file re-upload: " + e.getMessage());
                }

                // Process file asynchronously
                batchProcessingService.processBatchAsync(newBatchId, tempFilePath.toString());

                return BatchUploadResponseDTO.builder()
                                .batchId(newBatchId)
                                .totalCases(batch.getTotalCases())
                                .status(batch.getStatus().name())
                                .validationJobId(batch.getValidationJobId())
                                .build();
        }

        private String generateBatchId() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                return "BATCH_" + LocalDateTime.now().format(formatter);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] exportFailedCases(String batchId) {
                log.info("Exporting failed cases for batch: {}", batchId);

                List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

                if (errors.isEmpty()) {
                        throw new BusinessException("No errors found for batch: " + batchId);
                }

                return csvExporter.exportBatchErrors(errors);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] exportBatchCases(String batchId) {
                log.info("Exporting ALL cases (success + failure) for batch: {}", batchId);

                // Verify batch exists
                if (!caseBatchRepository.existsByBatchId(batchId)) {
                        throw new BusinessException("Batch not found: " + batchId);
                }

                // Find all ACTIVE/SUCCESS cases for this batch (status=200)
                List<Case> successCases = caseRepository.findAll().stream()
                                .filter(c -> batchId.equals(c.getImportBatchId()) && Integer.valueOf(200).equals(c.getStatus()))
                                .collect(Collectors.toList());

                // Find all errors for this batch
                List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

                // Check if there's anything to export
                if (successCases.isEmpty() && errors.isEmpty()) {
                        throw new BusinessException("No records found for batch: " + batchId);
                }

                log.info("Exporting batch {}: {} success cases, {} failure records",
                         batchId, successCases.size(), errors.size());

                // Export all data - success cases with STATUS="SUCCESS", errors with STATUS="FAILURE"
                return csvExporter.exportAllBatchData(successCases, errors);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "intakeReport", key = "#startDate + '-' + #endDate")
        public IntakeReportDTO getIntakeReport(String startDate, String endDate) {
                log.info("Generating intake report from {} to {}", startDate, endDate);

                // Parse dates
                LocalDateTime startDateTime = parseDate(startDate, true);
                LocalDateTime endDateTime = parseDate(endDate, false);

                // Get overall stats
                List<CaseBatch> batches = caseBatchRepository.findBatchesByDateRange(startDateTime, endDateTime);

                long totalReceived = batches.stream().mapToLong(CaseBatch::getTotalCases).sum();
                long totalValidated = batches.stream().mapToLong(CaseBatch::getValidCases).sum();
                long totalFailed = batches.stream().mapToLong(CaseBatch::getInvalidCases).sum();

                double successRate = totalReceived > 0 ? (totalValidated * 100.0 / totalReceived) : 0.0;

                // Get daily breakdown
                List<Object[]> dailyStats = caseBatchRepository.getDailyIntakeStats(startDateTime, endDateTime);
                List<IntakeReportItemDTO> dailyBreakdown = dailyStats.stream()
                                .map(row -> {
                                        java.sql.Date sqlDate = (java.sql.Date) row[0];
                                        long total = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                                        long validated = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                                        long failed = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                                        double rate = total > 0 ? (validated * 100.0 / total) : 0.0;

                                        return IntakeReportItemDTO.builder()
                                                        .date(sqlDate.toLocalDate())
                                                        .totalReceived(total)
                                                        .validated(validated)
                                                        .failed(failed)
                                                        .successRate(Math.round(rate * 100.0) / 100.0)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Get source-wise breakdown
                List<Object[]> sourceStats = caseBatchRepository.getSourceWiseIntakeStats(startDateTime, endDateTime);
                List<SourceWiseIntakeDTO> sourceBreakdown = sourceStats.stream()
                                .map(row -> {
                                        String source = row[0].toString();
                                        long total = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                                        long validated = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                                        long failed = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                                        double rate = total > 0 ? (validated * 100.0 / total) : 0.0;

                                        return SourceWiseIntakeDTO.builder()
                                                        .source(source)
                                                        .totalReceived(total)
                                                        .validated(validated)
                                                        .failed(failed)
                                                        .successRate(Math.round(rate * 100.0) / 100.0)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                return IntakeReportDTO.builder()
                                .startDate(startDateTime.toLocalDate())
                                .endDate(endDateTime.toLocalDate())
                                .totalReceived(totalReceived)
                                .totalValidated(totalValidated)
                                .totalFailed(totalFailed)
                                .successRate(Math.round(successRate * 100.0) / 100.0)
                                .dailyBreakdown(dailyBreakdown)
                                .sourceBreakdown(sourceBreakdown)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "unallocatedCasesReport", key = "#startDate + '-' + #endDate")
        public UnallocatedReportDTO getUnallocatedCasesReport(String startDate, String endDate) {
                log.info("Generating unallocated cases report from {} to {}", startDate, endDate);

                // Parse dates
                LocalDateTime startDateTime = parseDate(startDate, true);
                LocalDateTime endDateTime = parseDate(endDate, false);

                // Get total unallocated count
                Long totalUnallocated = caseRepository.countUnallocatedCasesByDateRange(startDateTime, endDateTime);

                // Get detailed breakdown
                List<Object[]> detailedStats = caseRepository
                                .getUnallocatedCasesGroupedByDateBucketSource(startDateTime, endDateTime);
                List<UnallocatedReportItemDTO> breakdown = detailedStats.stream()
                                .map(row -> {
                                        java.sql.Date sqlDate = (java.sql.Date) row[0];
                                        long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                                        String bucket = row[2] != null ? row[2].toString() : "UNKNOWN";
                                        String batchId = row[3] != null ? row[3].toString() : "UNKNOWN";

                                        return UnallocatedReportItemDTO.builder()
                                                        .date(sqlDate.toLocalDate())
                                                        .unallocatedCount(count)
                                                        .bucket(bucket)
                                                        .source(batchId)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Get bucket-wise breakdown
                List<Object[]> bucketStats = caseRepository.getUnallocatedCasesByBucket(startDateTime, endDateTime);
                List<BucketWiseUnallocatedDTO> bucketBreakdown = bucketStats.stream()
                                .map(row -> BucketWiseUnallocatedDTO.builder()
                                                .bucket(row[0] != null ? row[0].toString() : "UNKNOWN")
                                                .count(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                                                .build())
                                .collect(Collectors.toList());

                // Get source-wise breakdown
                List<Object[]> sourceStats = caseRepository.getUnallocatedCasesBySource(startDateTime, endDateTime);
                List<SourceWiseUnallocatedDTO> sourceBreakdown = sourceStats.stream()
                                .map(row -> SourceWiseUnallocatedDTO.builder()
                                                .source(row[0] != null ? row[0].toString() : "UNKNOWN")
                                                .count(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                                                .build())
                                .collect(Collectors.toList());

                return UnallocatedReportDTO.builder()
                                .startDate(startDateTime.toLocalDate())
                                .endDate(endDateTime.toLocalDate())
                                .totalUnallocated(totalUnallocated != null ? totalUnallocated : 0L)
                                .breakdown(breakdown)
                                .bucketBreakdown(bucketBreakdown)
                                .sourceBreakdown(sourceBreakdown)
                                .build();
        }

        /**
         * Parse date string to LocalDateTime
         */
        private LocalDateTime parseDate(String dateStr, boolean startOfDay) {
                if (dateStr == null || dateStr.trim().isEmpty()) {
                        // Default to last 30 days if not provided
                        return startOfDay ? LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0)
                                        : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
                }

                try {
                        java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);
                        return startOfDay ? localDate.atStartOfDay() : localDate.atTime(23, 59, 59);
                } catch (Exception e) {
                        log.warn("Invalid date format: {}, using defaults", dateStr);
                        return startOfDay ? LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0)
                                        : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
                }
        }

        private UnallocatedCaseDTO mapToUnallocatedCaseDTO(Case caseEntity) {
                return UnallocatedCaseDTO.builder()
                                .id(caseEntity.getId())
                                .caseNumber(caseEntity.getCaseNumber())
                                .externalCaseId(caseEntity.getExternalCaseId())
                                .customer(CustomerDTO.builder()
                                                .id(caseEntity.getLoan().getPrimaryCustomer().getId())
                                                .name(caseEntity.getLoan().getPrimaryCustomer().getFullName())
                                                .mobile(caseEntity.getLoan().getPrimaryCustomer().getMobileNumber())
                                                .build())
                                .loanDetails(LoanDetailsDTO.builder()
                                                .totalOutstanding(caseEntity.getLoan().getTotalOutstanding())
                                                .dpd(caseEntity.getLoan().getDpd())
                                                .bucket(caseEntity.getLoan().getBucket())
                                                .build())
                                .status(caseEntity.getCaseStatus())
                                .createdAt(caseEntity.getCreatedAt())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<CaseSearchResultDTO> searchCases(CaseSearchRequest request, Pageable pageable) {
                log.info("Searching cases with request: {}", request);

                // TODO: Implement advanced case search with dynamic query building
                // For now, return empty page to allow compilation
                return Page.empty(pageable);
        }

        @Override
        @Transactional(readOnly = true)
        public CaseTimelineDTO getCaseTimeline(Long caseId) {
                log.info("Fetching case timeline for caseId: {}", caseId);

                // Verify case exists
                Case caseEntity = caseRepository.findById(caseId)
                                .orElseThrow(() -> new BusinessException("Case not found: " + caseId));

                // TODO: Implement timeline event aggregation from multiple sources
                // (calls, PTPs, payments, notes, communications, etc.)
                // For now, return basic structure to allow compilation
                return CaseTimelineDTO.builder()
                                .caseId(caseEntity.getId())
                                .caseNumber(caseEntity.getCaseNumber())
                                .customerName(caseEntity.getLoan().getPrimaryCustomer().getFullName())
                                .loanAccountNumber(caseEntity.getLoan().getLoanAccountNumber())
                                .events(new ArrayList<>())
                                .summary(TimelineSummaryDTO.builder()
                                                .totalEvents(0)
                                                .totalCalls(0)
                                                .totalPTPs(0)
                                                .totalPayments(0)
                                                .totalNotes(0)
                                                .totalMessages(0)
                                                .connectedCalls(0)
                                                .failedCalls(0)
                                                .activePTPs(0)
                                                .keptPTPs(0)
                                                .brokenPTPs(0)
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<RecentUploadDTO> getAllBatches(String status, Pageable pageable) {
                log.info("Fetching all batches with status filter: {}", status);

                Page<CaseBatch> batchPage;

                if (status != null && !status.trim().isEmpty()) {
                        try {
                                BatchStatus batchStatus = BatchStatus.valueOf(status.toUpperCase());
                                batchPage = caseBatchRepository.findByStatusOrderByCreatedAtDesc(batchStatus, pageable);
                        } catch (IllegalArgumentException e) {
                                log.warn("Invalid batch status: {}. Returning all batches.", status);
                                batchPage = caseBatchRepository.findAllByOrderByCreatedAtDesc(pageable);
                        }
                } else {
                        batchPage = caseBatchRepository.findAllByOrderByCreatedAtDesc(pageable);
                }

                return batchPage.map(batch -> RecentUploadDTO.builder()
                                .batchId(batch.getBatchId())
                                .source(batch.getSourceType().name())
                                .uploadedBy(batch.getUploadedBy())
                                .uploadedAt(batch.getCreatedAt())
                                .totalCases(batch.getTotalCases())
                                .status(batch.getStatus().name())
                                .build());
        }

        // NOTE: Case Closure operations moved to Collections Service (CycleClosureService)
}

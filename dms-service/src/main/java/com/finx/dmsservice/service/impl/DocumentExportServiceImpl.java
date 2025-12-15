package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.domain.dto.*;
import com.finx.dmsservice.domain.entity.DocumentExportJob;
import com.finx.dmsservice.domain.enums.ExportFormat;
import com.finx.dmsservice.domain.enums.ExportType;
import com.finx.dmsservice.domain.enums.JobStatus;
import com.finx.dmsservice.exception.BusinessException;
import com.finx.dmsservice.exception.ResourceNotFoundException;
import com.finx.dmsservice.mapper.DocumentMapper;
import com.finx.dmsservice.repository.DocumentExportJobRepository;
import com.finx.dmsservice.service.AuditLogService;
import com.finx.dmsservice.service.DocumentExportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentExportServiceImpl implements DocumentExportService {

    private final DocumentExportJobRepository exportJobRepository;
    private final DocumentMapper mapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    private String toJson(Map<String, Object> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize map to JSON", e);
            return null;
        }
    }

    private String generateJobId() {
        return "EXP-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    // ==================== JOB CREATION ====================

    @Override
    public DocumentExportJobDTO createExportJob(CreateExportJobRequest request, Long requestedBy) {
        log.info("Creating export job for user: {}", requestedBy);

        String jobId = generateJobId();
        String documentIdsJson = request.getDocumentIds() != null ?
                request.getDocumentIds().toString() : null;

        DocumentExportJob job = DocumentExportJob.builder()
                .jobId(jobId)
                .exportType(request.getExportType())
                .exportFormat(request.getExportFormat() != null ? request.getExportFormat() : ExportFormat.ZIP)
                .documentIds(documentIdsJson)
                .filterCriteria(toJson(request.getFilterCriteria()))
                .jobStatus(JobStatus.PENDING)
                .createdBy(requestedBy)
                .totalDocuments(request.getDocumentIds() != null ? request.getDocumentIds().size() : 0)
                .exportedDocuments(0)
                .failedDocuments(0)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        job = exportJobRepository.save(job);

        auditLogService.logExportEvent(job.getId(), "EXPORT_JOB_CREATED", requestedBy,
                "Export job created: " + jobId + " with " + job.getTotalDocuments() + " documents");

        log.info("Created export job: {}", jobId);
        return mapper.toDto(job);
    }

    @Override
    public DocumentExportJobDTO createSingleExport(SingleExportRequest request, Long userId) {
        log.info("Creating single export for document: {} by user: {}", request.getDocumentId(), userId);

        String jobId = generateJobId();

        DocumentExportJob job = DocumentExportJob.builder()
                .jobId(jobId)
                .exportType(ExportType.SINGLE)
                .exportFormat(request.getFormat() != null ? request.getFormat() : ExportFormat.ORIGINAL)
                .documentIds("[" + request.getDocumentId() + "]")
                .jobStatus(JobStatus.PENDING)
                .createdBy(userId)
                .totalDocuments(1)
                .exportedDocuments(0)
                .failedDocuments(0)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // Store processing options in filter criteria
        Map<String, Object> options = new HashMap<>();
        if (request.getConvertToPdf() != null) options.put("convertToPdf", request.getConvertToPdf());
        if (request.getAddWatermark() != null) options.put("addWatermark", request.getAddWatermark());
        if (request.getWatermarkText() != null) options.put("watermarkText", request.getWatermarkText());
        if (request.getPasswordProtect() != null) options.put("passwordProtect", request.getPasswordProtect());
        if (request.getCompressImages() != null) options.put("compressImages", request.getCompressImages());
        if (request.getQuality() != null) options.put("quality", request.getQuality());

        if (!options.isEmpty()) {
            job.setFilterCriteria(toJson(options));
        }

        job = exportJobRepository.save(job);

        auditLogService.logExportEvent(job.getId(), "SINGLE_EXPORT_CREATED", userId,
                "Single export job created for document: " + request.getDocumentId());

        log.info("Created single export job: {}", jobId);
        return mapper.toDto(job);
    }

    @Override
    public DocumentExportJobDTO createBulkExport(BulkExportRequest request, Long userId) {
        log.info("Creating bulk export for {} documents by user: {}",
                request.getDocumentIds() != null ? request.getDocumentIds().size() : 0, userId);

        if (request.getDocumentIds() == null || request.getDocumentIds().isEmpty()) {
            throw new BusinessException("At least one document ID is required for bulk export");
        }

        String jobId = generateJobId();
        String documentIdsJson = request.getDocumentIds().toString();

        DocumentExportJob job = DocumentExportJob.builder()
                .jobId(jobId)
                .exportType(ExportType.BULK)
                .exportFormat(request.getFormat() != null ? request.getFormat() : ExportFormat.ZIP)
                .documentIds(documentIdsJson)
                .jobStatus(JobStatus.PENDING)
                .createdBy(userId)
                .totalDocuments(request.getDocumentIds().size())
                .exportedDocuments(0)
                .failedDocuments(0)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // Store processing options in filter criteria
        Map<String, Object> options = new HashMap<>();
        if (request.getCreateZip() != null) options.put("createZip", request.getCreateZip());
        if (request.getZipFileName() != null) options.put("zipFileName", request.getZipFileName());
        if (request.getIncludeMetadata() != null) options.put("includeMetadata", request.getIncludeMetadata());
        if (request.getMetadataFormat() != null) options.put("metadataFormat", request.getMetadataFormat());
        if (request.getOrganizeByType() != null) options.put("organizeByType", request.getOrganizeByType());
        if (request.getOrganizeByDate() != null) options.put("organizeByDate", request.getOrganizeByDate());
        if (request.getConvertToPdf() != null) options.put("convertToPdf", request.getConvertToPdf());
        if (request.getAddWatermark() != null) options.put("addWatermark", request.getAddWatermark());
        if (request.getWatermarkText() != null) options.put("watermarkText", request.getWatermarkText());
        if (request.getNotifyEmail() != null) options.put("notifyEmail", request.getNotifyEmail());
        if (request.getNotifyOnComplete() != null) options.put("notifyOnComplete", request.getNotifyOnComplete());
        if (request.getSearchCriteria() != null) options.put("searchCriteria", request.getSearchCriteria());

        if (!options.isEmpty()) {
            job.setFilterCriteria(toJson(options));
        }

        job = exportJobRepository.save(job);

        auditLogService.logExportEvent(job.getId(), "BULK_EXPORT_CREATED", userId,
                "Bulk export job created with " + request.getDocumentIds().size() + " documents");

        log.info("Created bulk export job: {}", jobId);
        return mapper.toDto(job);
    }

    // ==================== JOB RETRIEVAL ====================

    @Override
    @Transactional(readOnly = true)
    public DocumentExportJobDTO getExportJobById(Long id) {
        DocumentExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));
        return mapper.toDto(job);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentExportJobDTO getExportJobByJobId(String jobId) {
        DocumentExportJob job = exportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with jobId: " + jobId));
        return mapper.toDto(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentExportJobDTO> getExportJobsByUser(Long userId) {
        return exportJobRepository.findByCreatedBy(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentExportJobDTO> getAllExportJobs(Pageable pageable) {
        return exportJobRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentExportJobDTO> getExportJobsByStatus(JobStatus status, Pageable pageable) {
        return exportJobRepository.findByJobStatus(status, pageable).map(mapper::toDto);
    }

    // ==================== JOB PROCESSING ====================

    @Override
    @Async
    public DocumentExportJobDTO processExportJob(Long id) {
        DocumentExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));

        if (job.getJobStatus() != JobStatus.PENDING) {
            throw new BusinessException("Export job is not in PENDING status");
        }

        log.info("Processing export job: {}", job.getJobId());
        job.setJobStatus(JobStatus.PROCESSING);
        job.setStartedAt(LocalDateTime.now());
        job = exportJobRepository.save(job);

        auditLogService.logExportEvent(job.getId(), "EXPORT_PROCESSING_STARTED", job.getCreatedBy(),
                "Started processing export job: " + job.getJobId());

        try {
            // Simulate processing
            int totalDocs = job.getTotalDocuments() != null ? job.getTotalDocuments() : 0;
            int failedCount = 0;

            for (int i = 0; i < totalDocs; i++) {
                // Process each document (placeholder for actual implementation)
                job.setExportedDocuments(i + 1);
                exportJobRepository.save(job);
            }

            // Generate export file URL
            String exportFileUrl = "/exports/" + job.getJobId() +
                    (job.getExportFormat() == ExportFormat.ZIP ? ".zip" :
                     job.getExportFormat() == ExportFormat.PDF_MERGED ? ".pdf" : "");
            job.setExportFileUrl(exportFileUrl);
            job.setExportFileSizeBytes(1024L * totalDocs); // Placeholder size
            job.setFailedDocuments(failedCount);
            job.setJobStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            auditLogService.logExportEvent(job.getId(), "EXPORT_COMPLETED", job.getCreatedBy(),
                    "Export job completed: " + job.getJobId() +
                    " - Exported: " + job.getExportedDocuments() + ", Failed: " + failedCount);

            log.info("Completed export job: {}", job.getJobId());
        } catch (Exception e) {
            log.error("Error processing export job: {}", e.getMessage());
            job.setJobStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());

            auditLogService.logExportEvent(job.getId(), "EXPORT_FAILED", job.getCreatedBy(),
                    "Export job failed: " + job.getJobId() + " - Error: " + e.getMessage());
        }

        job = exportJobRepository.save(job);
        return mapper.toDto(job);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportProgressDTO getExportProgress(String jobId) {
        DocumentExportJob job = exportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with jobId: " + jobId));

        int total = job.getTotalDocuments() != null ? job.getTotalDocuments() : 0;
        int processed = job.getExportedDocuments() != null ? job.getExportedDocuments() : 0;
        int failed = job.getFailedDocuments() != null ? job.getFailedDocuments() : 0;

        double percentage = total > 0 ? (double) processed / total * 100 : 0;

        Long elapsedSeconds = null;
        Long remainingSeconds = null;
        LocalDateTime estimatedCompletion = null;

        if (job.getStartedAt() != null) {
            LocalDateTime endTime = job.getCompletedAt() != null ? job.getCompletedAt() : LocalDateTime.now();
            elapsedSeconds = ChronoUnit.SECONDS.between(job.getStartedAt(), endTime);

            if (job.getJobStatus() == JobStatus.PROCESSING && processed > 0) {
                double avgTimePerDoc = (double) elapsedSeconds / processed;
                int remaining = total - processed;
                remainingSeconds = (long) (avgTimePerDoc * remaining);
                estimatedCompletion = LocalDateTime.now().plusSeconds(remainingSeconds);
            }
        }

        return ExportProgressDTO.builder()
                .jobId(jobId)
                .status(job.getJobStatus())
                .totalDocuments(total)
                .processedDocuments(processed)
                .successCount(processed - failed)
                .failureCount(failed)
                .progressPercentage(percentage)
                .currentDocument(job.getJobStatus() == JobStatus.PROCESSING ? "Processing document " + (processed + 1) : null)
                .startedAt(job.getStartedAt())
                .estimatedCompletion(estimatedCompletion)
                .elapsedTimeSeconds(elapsedSeconds)
                .remainingTimeSeconds(remainingSeconds)
                .errors(new ArrayList<>()) // Placeholder - would be populated from actual errors
                .build();
    }

    @Override
    public void cancelExportJob(Long id) {
        DocumentExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));

        if (job.getJobStatus() == JobStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel completed export job");
        }

        JobStatus previousStatus = job.getJobStatus();
        job.setJobStatus(JobStatus.FAILED);
        job.setErrorMessage("Cancelled by user");
        job.setCompletedAt(LocalDateTime.now());
        exportJobRepository.save(job);

        auditLogService.logExportEvent(job.getId(), "EXPORT_CANCELLED", job.getCreatedBy(),
                "Export job cancelled from status " + previousStatus + ": " + job.getJobId());

        log.info("Cancelled export job: {}", job.getJobId());
    }

    @Override
    public void deleteExportJob(Long id) {
        DocumentExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));

        Long createdBy = job.getCreatedBy();
        String jobIdStr = job.getJobId();

        // TODO: Delete actual export files from storage
        exportJobRepository.delete(job);

        auditLogService.logExportEvent(id, "EXPORT_JOB_DELETED", createdBy,
                "Export job deleted: " + jobIdStr);

        log.info("Deleted export job: {}", jobIdStr);
    }

    @Override
    public DocumentExportJobDTO retryFailedJob(Long id, Long userId) {
        DocumentExportJob originalJob = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));

        if (originalJob.getJobStatus() != JobStatus.FAILED) {
            throw new BusinessException("Only failed jobs can be retried");
        }

        // Create a new job with the same parameters
        String newJobId = generateJobId();

        DocumentExportJob newJob = DocumentExportJob.builder()
                .jobId(newJobId)
                .exportType(originalJob.getExportType())
                .exportFormat(originalJob.getExportFormat())
                .documentIds(originalJob.getDocumentIds())
                .filterCriteria(originalJob.getFilterCriteria())
                .jobStatus(JobStatus.PENDING)
                .createdBy(userId)
                .totalDocuments(originalJob.getTotalDocuments())
                .exportedDocuments(0)
                .failedDocuments(0)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        newJob = exportJobRepository.save(newJob);

        auditLogService.logExportEvent(newJob.getId(), "EXPORT_JOB_RETRIED", userId,
                "Retried failed export job " + originalJob.getJobId() + " as new job: " + newJobId);

        log.info("Created retry job: {} from original job: {}", newJobId, originalJob.getJobId());
        return mapper.toDto(newJob);
    }

    // ==================== DOWNLOAD ====================

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadExportFile(Long id) {
        return downloadExportFile(id, null);
    }

    @Override
    public byte[] downloadExportFile(Long id, Long userId) {
        DocumentExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));

        if (job.getJobStatus() != JobStatus.COMPLETED) {
            throw new BusinessException("Export job is not completed yet");
        }

        if (job.getExpiresAt() != null && LocalDateTime.now().isAfter(job.getExpiresAt())) {
            throw new BusinessException("Export file has expired");
        }

        if (userId != null) {
            auditLogService.logExportEvent(job.getId(), "EXPORT_FILE_DOWNLOADED", userId,
                    "Downloaded export file for job: " + job.getJobId());
        }

        // TODO: Implement actual file download from storage
        log.info("Downloading export file for job: {}", job.getJobId());
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public String getDownloadUrl(Long id, Long userId) {
        DocumentExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found with id: " + id));

        if (job.getJobStatus() != JobStatus.COMPLETED) {
            throw new BusinessException("Export job is not completed yet");
        }

        if (job.getExpiresAt() != null && LocalDateTime.now().isAfter(job.getExpiresAt())) {
            throw new BusinessException("Export file has expired");
        }

        // TODO: Generate actual pre-signed URL from storage service
        String downloadUrl = "/api/dms/export/" + id + "/download?token=" +
                UUID.randomUUID().toString();

        auditLogService.logExportEvent(job.getId(), "DOWNLOAD_URL_GENERATED", userId,
                "Generated download URL for job: " + job.getJobId());

        log.info("Generated download URL for job: {}", job.getJobId());
        return downloadUrl;
    }

    // ==================== HISTORY ====================

    @Override
    @Transactional(readOnly = true)
    public Page<ExportHistoryDTO> getExportHistory(Long userId, Pageable pageable) {
        Page<DocumentExportJob> jobs = exportJobRepository.findHistoryByUser(userId, pageable);
        return jobs.map(this::mapToHistoryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExportHistoryDTO> getExportHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<DocumentExportJob> jobs = exportJobRepository.findByCompletedDateRange(startDate, endDate, pageable);
        return jobs.map(this::mapToHistoryDTO);
    }

    private ExportHistoryDTO mapToHistoryDTO(DocumentExportJob job) {
        Long durationSeconds = null;
        if (job.getStartedAt() != null && job.getCompletedAt() != null) {
            durationSeconds = ChronoUnit.SECONDS.between(job.getStartedAt(), job.getCompletedAt());
        }

        boolean isExpired = job.getExpiresAt() != null && LocalDateTime.now().isAfter(job.getExpiresAt());

        return ExportHistoryDTO.builder()
                .id(job.getId())
                .jobId(job.getJobId())
                .exportType(job.getExportType())
                .exportFormat(job.getExportFormat())
                .status(job.getJobStatus())
                .totalDocuments(job.getTotalDocuments())
                .exportedDocuments(job.getExportedDocuments())
                .failedDocuments(job.getFailedDocuments())
                .fileSizeBytes(job.getExportFileSizeBytes())
                .fileSizeFormatted(formatFileSize(job.getExportFileSizeBytes()))
                .isDownloaded(false) // Would be tracked separately
                .downloadCount(0) // Would be tracked separately
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .durationSeconds(durationSeconds)
                .expiresAt(job.getExpiresAt())
                .isExpired(isExpired)
                .createdAt(job.getCreatedAt())
                .createdBy(job.getCreatedBy())
                .createdByName(null) // Would be fetched from user service
                .build();
    }

    // ==================== SUMMARY ====================

    @Override
    @Transactional(readOnly = true)
    public ExportSummaryDTO getExportSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Count by status
        long total = exportJobRepository.count();
        long pending = exportJobRepository.countByJobStatus(JobStatus.PENDING);
        long processing = exportJobRepository.countByJobStatus(JobStatus.PROCESSING);
        long completed = exportJobRepository.countByJobStatus(JobStatus.COMPLETED);
        long failed = exportJobRepository.countByJobStatus(JobStatus.FAILED);

        // Today's stats
        long todayCreated = exportJobRepository.countTodayCreated(startOfDay);
        long todayCompleted = exportJobRepository.countTodayCompleted(startOfDay);
        long todayFailed = exportJobRepository.countTodayFailed(startOfDay);

        // Aggregations
        long totalDocsExported = exportJobRepository.sumTotalDocumentsExported();
        long totalDocsFailed = exportJobRepository.sumTotalDocumentsFailed();
        long totalExportSize = exportJobRepository.sumTotalExportSize();
        long expiredFiles = exportJobRepository.countExpiredFiles(now);

        // Jobs by format
        Map<String, Long> jobsByFormat = new HashMap<>();
        for (ExportFormat format : ExportFormat.values()) {
            jobsByFormat.put(format.name(), exportJobRepository.countByExportFormat(format));
        }

        // Jobs by type
        Map<String, Long> jobsByType = new HashMap<>();
        for (ExportType type : ExportType.values()) {
            jobsByType.put(type.name(), exportJobRepository.countByExportType(type));
        }

        return ExportSummaryDTO.builder()
                .totalJobs(total)
                .pendingJobs(pending)
                .processingJobs(processing)
                .completedJobs(completed)
                .failedJobs(failed)
                .cancelledJobs(0L) // No cancelled status, tracked as failed
                .todayCreated(todayCreated)
                .todayCompleted(todayCompleted)
                .todayFailed(todayFailed)
                .totalDocumentsExported(totalDocsExported)
                .totalDocumentsFailed(totalDocsFailed)
                .totalExportSizeBytes(totalExportSize)
                .totalExportSizeFormatted(formatFileSize(totalExportSize))
                .averageDurationSeconds(0L) // Would need custom query
                .jobsByFormat(jobsByFormat)
                .jobsByType(jobsByType)
                .activeDownloads(processing)
                .expiredFiles(expiredFiles)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExportSummaryDTO getExportSummaryByUser(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Count by status for user
        long total = exportJobRepository.countByUser(userId);
        long pending = exportJobRepository.countByUserAndStatus(userId, JobStatus.PENDING);
        long processing = exportJobRepository.countByUserAndStatus(userId, JobStatus.PROCESSING);
        long completed = exportJobRepository.countByUserAndStatus(userId, JobStatus.COMPLETED);
        long failed = exportJobRepository.countByUserAndStatus(userId, JobStatus.FAILED);

        // Today's stats for user
        long todayCreated = exportJobRepository.countTodayCreatedByUser(userId, startOfDay);
        long todayCompleted = exportJobRepository.countTodayCompletedByUser(userId, startOfDay);
        long todayFailed = exportJobRepository.countTodayFailedByUser(userId, startOfDay);

        // Aggregations for user
        long totalDocsExported = exportJobRepository.sumTotalDocumentsExportedByUser(userId);
        long totalDocsFailed = exportJobRepository.sumTotalDocumentsFailedByUser(userId);
        long totalExportSize = exportJobRepository.sumTotalExportSizeByUser(userId);
        long expiredFiles = exportJobRepository.countExpiredFilesByUser(userId, now);

        // Jobs by format for user
        Map<String, Long> jobsByFormat = new HashMap<>();
        for (ExportFormat format : ExportFormat.values()) {
            jobsByFormat.put(format.name(), exportJobRepository.countByUserAndFormat(userId, format));
        }

        // Jobs by type for user
        Map<String, Long> jobsByType = new HashMap<>();
        for (ExportType type : ExportType.values()) {
            jobsByType.put(type.name(), exportJobRepository.countByUserAndType(userId, type));
        }

        return ExportSummaryDTO.builder()
                .totalJobs(total)
                .pendingJobs(pending)
                .processingJobs(processing)
                .completedJobs(completed)
                .failedJobs(failed)
                .cancelledJobs(0L)
                .todayCreated(todayCreated)
                .todayCompleted(todayCompleted)
                .todayFailed(todayFailed)
                .totalDocumentsExported(totalDocsExported)
                .totalDocumentsFailed(totalDocsFailed)
                .totalExportSizeBytes(totalExportSize)
                .totalExportSizeFormatted(formatFileSize(totalExportSize))
                .averageDurationSeconds(0L)
                .jobsByFormat(jobsByFormat)
                .jobsByType(jobsByType)
                .activeDownloads(processing)
                .expiredFiles(expiredFiles)
                .build();
    }

    // ==================== CLEANUP ====================

    @Override
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void cleanupExpiredJobs() {
        log.info("Cleaning up expired export jobs");
        List<DocumentExportJob> expiredJobs = exportJobRepository.findExpiredJobs(LocalDateTime.now());

        for (DocumentExportJob job : expiredJobs) {
            // TODO: Delete actual export files from storage
            exportJobRepository.delete(job);
            log.info("Deleted expired export job: {}", job.getJobId());
        }

        log.info("Cleaned up {} expired export jobs", expiredJobs.size());
    }
}

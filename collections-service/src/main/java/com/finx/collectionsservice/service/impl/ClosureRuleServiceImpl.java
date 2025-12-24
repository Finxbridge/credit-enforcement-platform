package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.entity.ClosureRule;
import com.finx.collectionsservice.domain.entity.ClosureRuleExecution;
import com.finx.collectionsservice.domain.entity.CycleClosure;
import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import com.finx.collectionsservice.domain.enums.ClosureStatus;
import com.finx.collectionsservice.domain.enums.RuleExecutionStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.repository.ClosureRuleExecutionRepository;
import com.finx.collectionsservice.repository.ClosureRuleRepository;
import com.finx.collectionsservice.repository.CycleClosureRepository;
import com.finx.collectionsservice.service.ClosureRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosureRuleServiceImpl implements ClosureRuleService {

    private final ClosureRuleRepository ruleRepository;
    private final ClosureRuleExecutionRepository executionRepository;
    private final CycleClosureRepository closureRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public ClosureRuleDTO createRule(CreateClosureRuleRequest request, Long userId) {
        log.info("Creating closure rule: {}", request.getRuleName());

        // Generate rule code
        String ruleCode = generateRuleCode(request.getRuleType());

        // Validate cron expression if provided
        if (request.getCronExpression() != null && !request.getCronExpression().isBlank()) {
            if (!validateCronExpression(request.getCronExpression())) {
                throw new BusinessException("Invalid cron expression: " + request.getCronExpression());
            }
        }

        ClosureRule rule = ClosureRule.builder()
                .ruleCode(ruleCode)
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .ruleType(request.getRuleType())
                .cronExpression(request.getCronExpression())
                .isScheduled(request.getIsScheduled() != null ? request.getIsScheduled() : false)
                .closureReason(request.getClosureReason())
                .minZeroOutstandingDays(request.getMinZeroOutstandingDays())
                .minInactivityDays(request.getMinInactivityDays())
                .includeBuckets(request.getIncludeBuckets())
                .excludeStatuses(request.getExcludeStatuses())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .priority(request.getPriority() != null ? request.getPriority() : 10)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        rule = ruleRepository.save(rule);
        log.info("Created closure rule with ID: {}", rule.getId());

        return mapToDTO(rule);
    }

    @Override
    @Transactional
    public ClosureRuleDTO updateRule(Long ruleId, CreateClosureRuleRequest request, Long userId) {
        log.info("Updating closure rule ID: {}", ruleId);

        ClosureRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule", ruleId));

        // Validate cron expression if provided
        if (request.getCronExpression() != null && !request.getCronExpression().isBlank()) {
            if (!validateCronExpression(request.getCronExpression())) {
                throw new BusinessException("Invalid cron expression: " + request.getCronExpression());
            }
        }

        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setRuleType(request.getRuleType());
        rule.setCronExpression(request.getCronExpression());
        rule.setIsScheduled(request.getIsScheduled() != null ? request.getIsScheduled() : rule.getIsScheduled());
        rule.setClosureReason(request.getClosureReason());
        rule.setMinZeroOutstandingDays(request.getMinZeroOutstandingDays());
        rule.setMinInactivityDays(request.getMinInactivityDays());
        rule.setIncludeBuckets(request.getIncludeBuckets());
        rule.setExcludeStatuses(request.getExcludeStatuses());
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : rule.getIsActive());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : rule.getPriority());
        rule.setUpdatedBy(userId);

        rule = ruleRepository.save(rule);
        log.info("Updated closure rule ID: {}", ruleId);

        return mapToDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureRuleDTO getRuleById(Long ruleId) {
        ClosureRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule", ruleId));
        return mapToDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureRuleDTO getRuleByCode(String ruleCode) {
        ClosureRule rule = ruleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule with code: " + ruleCode));
        return mapToDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClosureRuleDTO> getActiveRules() {
        return ruleRepository.findActiveRulesOrderByPriority().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClosureRuleDTO> searchRules(String searchTerm, ClosureRuleType ruleType, Boolean isActive, Pageable pageable) {
        return ruleRepository.searchRules(searchTerm, ruleType, isActive, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public ClosureRuleDTO activateRule(Long ruleId, Long userId) {
        ClosureRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule", ruleId));

        rule.setIsActive(true);
        rule.setUpdatedBy(userId);
        rule = ruleRepository.save(rule);

        log.info("Activated closure rule ID: {}", ruleId);
        return mapToDTO(rule);
    }

    @Override
    @Transactional
    public ClosureRuleDTO deactivateRule(Long ruleId, Long userId) {
        ClosureRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule", ruleId));

        rule.setIsActive(false);
        rule.setUpdatedBy(userId);
        rule = ruleRepository.save(rule);

        log.info("Deactivated closure rule ID: {}", ruleId);
        return mapToDTO(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        if (!ruleRepository.existsById(ruleId)) {
            throw new ResourceNotFoundException("Closure Rule", ruleId);
        }
        ruleRepository.deleteById(ruleId);
        log.info("Deleted closure rule ID: {}", ruleId);
    }

    @Override
    @Transactional
    public SimulationResultDTO simulateRule(Long ruleId) {
        log.info("Simulating rule ID: {}", ruleId);

        ClosureRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule", ruleId));

        // Fetch eligible cases from database
        List<EligibleCaseData> eligibleCases = fetchEligibleCases(rule);

        // Create simulation execution record
        String executionId = UUID.randomUUID().toString();
        ClosureRuleExecution execution = ClosureRuleExecution.builder()
                .executionId(executionId)
                .rule(rule)
                .status(RuleExecutionStatus.COMPLETED)
                .isSimulation(true)
                .totalEligible(eligibleCases.size())
                .totalProcessed(eligibleCases.size())
                .totalSuccess(0)
                .totalFailed(0)
                .totalSkipped(0)
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .durationMs(0L)
                .triggeredBy("MANUAL_SIMULATION")
                .build();
        executionRepository.save(execution);

        // Build result
        BigDecimal totalOutstanding = eligibleCases.stream()
                .map(c -> c.getOutstandingAmount() != null ? c.getOutstandingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SimulationResultDTO.EligibleCaseDTO> eligibleCaseDTOs = eligibleCases.stream()
                .map(this::mapToEligibleCaseDTO)
                .collect(Collectors.toList());

        return SimulationResultDTO.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .closureReason(rule.getClosureReason())
                .totalEligibleCases(eligibleCases.size())
                .totalOutstandingAmount(totalOutstanding)
                .eligibleCases(eligibleCaseDTOs)
                .simulatedAt(LocalDateTime.now().format(DATE_FORMATTER))
                .build();
    }

    @Override
    @Transactional
    public RuleExecutionDTO executeRule(Long ruleId, Long userId) {
        log.info("Executing rule ID: {} by user: {}", ruleId, userId);

        ClosureRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Closure Rule", ruleId));

        String executionId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();

        // Create execution record
        ClosureRuleExecution execution = ClosureRuleExecution.builder()
                .executionId(executionId)
                .rule(rule)
                .status(RuleExecutionStatus.RUNNING)
                .isSimulation(false)
                .totalEligible(0)
                .totalProcessed(0)
                .totalSuccess(0)
                .totalFailed(0)
                .totalSkipped(0)
                .startedAt(startTime)
                .triggeredBy("MANUAL")
                .executedBy(userId)
                .build();
        execution = executionRepository.save(execution);

        try {
            // Fetch eligible cases from database
            List<EligibleCaseData> eligibleCases = fetchEligibleCases(rule);
            execution.setTotalEligible(eligibleCases.size());

            if (eligibleCases.isEmpty()) {
                execution.setStatus(RuleExecutionStatus.COMPLETED);
                execution.setCompletedAt(LocalDateTime.now());
                execution.setDurationMs(ChronoUnit.MILLIS.between(startTime, LocalDateTime.now()));
                executionRepository.save(execution);

                log.info("No eligible cases found for rule ID: {}", ruleId);
                return mapToExecutionDTO(execution);
            }

            // Process each case
            int success = 0;
            int failed = 0;
            int skipped = 0;
            StringBuilder logBuilder = new StringBuilder();

            for (EligibleCaseData caseData : eligibleCases) {
                try {
                    // Check if already closed in cycle_closure_cases
                    Optional<CycleClosure> existing = closureRepository.findByCaseId(caseData.getId());
                    if (existing.isPresent() && existing.get().getClosureStatus() == ClosureStatus.COMPLETED) {
                        skipped++;
                        logBuilder.append("Skipped case ").append(caseData.getCaseNumber()).append(" - already closed\n");
                        continue;
                    }

                    // Update the case status in cases table directly
                    boolean caseUpdated = closeCaseInDatabase(caseData.getId(), rule.getClosureReason(), userId);

                    if (!caseUpdated) {
                        failed++;
                        logBuilder.append("Failed case ").append(caseData.getCaseNumber()).append(" - could not update case status\n");
                        continue;
                    }

                    // Record the closure in cycle_closure_cases table
                    CycleClosure closure = CycleClosure.builder()
                            .executionId(executionId)
                            .caseId(caseData.getId())
                            .caseNumber(caseData.getCaseNumber())
                            .loanAccountNumber(caseData.getLoanAccountNumber())
                            .customerName(caseData.getCustomerName())
                            .dpdAtClosure(caseData.getDpd())
                            .bucketAtClosure(caseData.getBucket())
                            .outstandingAtClosure(caseData.getOutstandingAmount())
                            .statusBeforeClosure(caseData.getCaseStatus())
                            .closureStatus(ClosureStatus.COMPLETED)
                            .closureReason(rule.getClosureReason())
                            .archivedAt(LocalDateTime.now())
                            .build();
                    closureRepository.save(closure);

                    success++;
                    logBuilder.append("Closed case ").append(caseData.getCaseNumber()).append("\n");

                } catch (Exception e) {
                    failed++;
                    logBuilder.append("Failed case ").append(caseData.getCaseNumber()).append(": ").append(e.getMessage()).append("\n");
                    log.error("Failed to close case {}: {}", caseData.getId(), e.getMessage());
                }

                execution.setTotalProcessed(success + failed + skipped);
            }

            // Update execution
            execution.setTotalSuccess(success);
            execution.setTotalFailed(failed);
            execution.setTotalSkipped(skipped);
            execution.setTotalProcessed(success + failed + skipped);
            execution.setExecutionLog(logBuilder.toString());
            execution.setStatus(failed > 0 ? RuleExecutionStatus.PARTIALLY_COMPLETED : RuleExecutionStatus.COMPLETED);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setDurationMs(ChronoUnit.MILLIS.between(startTime, LocalDateTime.now()));

            // Update rule stats
            rule.setLastExecutedAt(LocalDateTime.now());
            rule.setLastExecutionCount(success);
            rule.setTotalCasesClosed(rule.getTotalCasesClosed() + success);
            ruleRepository.save(rule);

            executionRepository.save(execution);

            log.info("Rule {} execution completed: {} success, {} failed, {} skipped",
                    ruleId, success, failed, skipped);

            return mapToExecutionDTO(execution);

        } catch (Exception e) {
            log.error("Rule execution failed for rule {}: {}", ruleId, e.getMessage(), e);

            execution.setStatus(RuleExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.setCompletedAt(LocalDateTime.now());
            execution.setDurationMs(ChronoUnit.MILLIS.between(startTime, LocalDateTime.now()));
            executionRepository.save(execution);

            return mapToExecutionDTO(execution);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RuleExecutionDTO> getRuleExecutionHistory(Long ruleId, Pageable pageable) {
        return executionRepository.findByRuleIdOrderByCreatedAtDesc(ruleId, pageable)
                .map(this::mapToExecutionDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleExecutionDTO getExecutionById(String executionId) {
        ClosureRuleExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution", executionId));
        return mapToExecutionDTO(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleExecutionDTO> getRecentExecutions(int limit) {
        return executionRepository.findRecentExecutions(PageRequest.of(0, limit)).stream()
                .map(this::mapToExecutionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CycleClosureDashboardDTO getDashboardStats() {
        log.info("Fetching cycle closure dashboard stats");

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // Get counts
        Integer activeRulesCount = ruleRepository.countActiveRules();
        Long totalArchivedAllTime = executionRepository.sumTotalSuccessfulClosures();
        Long totalArchivedThisCycle = executionRepository.sumSuccessfulClosuresSince(monthStart);
        Long failedClosures = executionRepository.sumTotalFailedClosures();
        Long pendingClosures = executionRepository.countPendingExecutions();

        // Get eligible cases count using a simple query
        Long totalEligibleForArchival = 0L;
        try {
            String countSql = "SELECT COUNT(*) FROM cases c " +
                    "JOIN loan_details l ON c.loan_id = l.id " +
                    "WHERE c.status = 200 AND c.case_status != 'CLOSED' AND l.outstanding_amount = 0 " +
                    "AND NOT EXISTS (SELECT 1 FROM cycle_closure_cases cc WHERE cc.case_id = c.id AND cc.closure_status = 'COMPLETED')";
            totalEligibleForArchival = jdbcTemplate.queryForObject(countSql, Long.class);
        } catch (Exception e) {
            log.warn("Failed to fetch eligible cases count: {}", e.getMessage());
        }

        // Get archival by reason
        Map<String, Long> archivalByReason = new HashMap<>();
        closureRepository.findAll().stream()
                .filter(c -> c.getClosureReason() != null)
                .collect(Collectors.groupingBy(CycleClosure::getClosureReason, Collectors.counting()))
                .forEach(archivalByReason::put);

        // Get recent executions
        List<CycleClosureDashboardDTO.RecentExecutionDTO> recentExecutions = executionRepository
                .findRecentExecutions(PageRequest.of(0, 10)).stream()
                .map(e -> CycleClosureDashboardDTO.RecentExecutionDTO.builder()
                        .executionId(e.getExecutionId())
                        .ruleName(e.getRule() != null ? e.getRule().getRuleName() : "Unknown")
                        .casesArchived(e.getTotalSuccess())
                        .status(e.getStatus().name())
                        .executedAt(e.getCreatedAt().format(DATE_FORMATTER))
                        .build())
                .collect(Collectors.toList());

        return CycleClosureDashboardDTO.builder()
                .totalEligibleForArchival(totalEligibleForArchival != null ? totalEligibleForArchival : 0L)
                .totalArchivedThisCycle(totalArchivedThisCycle != null ? totalArchivedThisCycle : 0L)
                .totalArchivedAllTime(totalArchivedAllTime != null ? totalArchivedAllTime : 0L)
                .activeRulesCount(activeRulesCount != null ? activeRulesCount : 0)
                .pendingClosures(pendingClosures != null ? pendingClosures : 0L)
                .failedClosures(failedClosures != null ? failedClosures : 0L)
                .archivalByReason(archivalByReason)
                .recentExecutions(recentExecutions)
                .build();
    }

    @Override
    public boolean validateCronExpression(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getNextScheduledRun(String cronExpression) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            LocalDateTime next = cron.next(LocalDateTime.now());
            return next != null ? next.format(DATE_FORMATTER) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Eligible Case DTO for internal use
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class EligibleCaseData {
        private Long id;
        private String caseNumber;
        private String loanAccountNumber;
        private String customerName;
        private BigDecimal outstandingAmount;
        private Integer dpd;
        private String bucket;
        private String caseStatus;
        private LocalDateTime lastActivityDate;
        private LocalDateTime zeroOutstandingSince;
    }

    /**
     * Fetch eligible cases directly from database using JdbcTemplate
     * This queries the cases table with loan details joined
     */
    private List<EligibleCaseData> fetchEligibleCases(ClosureRule rule) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT c.id, c.case_number, l.loan_account_number, ");
            sql.append("COALESCE(cu.full_name, cu.first_name || ' ' || cu.last_name) as customer_name, ");
            sql.append("l.outstanding_amount, l.dpd, l.bucket, c.case_status, ");
            sql.append("c.updated_at as last_activity_date, ");
            sql.append("CASE WHEN l.outstanding_amount = 0 THEN c.updated_at ELSE NULL END as zero_outstanding_since ");
            sql.append("FROM cases c ");
            sql.append("JOIN loan_details l ON c.loan_id = l.id ");
            sql.append("LEFT JOIN customers cu ON l.primary_customer_id = cu.id ");
            sql.append("WHERE c.status = 200 "); // Active cases only
            sql.append("AND c.case_status != 'CLOSED' ");

            List<Object> params = new ArrayList<>();

            // Apply rule type specific filters
            switch (rule.getRuleType()) {
                case ZERO_OUTSTANDING:
                    sql.append("AND l.outstanding_amount = 0 ");
                    if (rule.getMinZeroOutstandingDays() != null && rule.getMinZeroOutstandingDays() > 0) {
                        sql.append("AND c.updated_at <= NOW() - INTERVAL '").append(rule.getMinZeroOutstandingDays()).append(" days' ");
                    }
                    break;
                case FULLY_SETTLED:
                    sql.append("AND l.outstanding_amount = 0 ");
                    sql.append("AND EXISTS (SELECT 1 FROM ots_requests o WHERE o.case_id = c.id AND o.status = 'COMPLETED') ");
                    break;
                case WRITTEN_OFF:
                    sql.append("AND c.case_status = 'WRITTEN_OFF' ");
                    break;
                case NO_ACTIVITY:
                    if (rule.getMinInactivityDays() != null && rule.getMinInactivityDays() > 0) {
                        sql.append("AND c.updated_at <= NOW() - INTERVAL '").append(rule.getMinInactivityDays()).append(" days' ");
                    }
                    break;
                case NPA_AGED:
                    sql.append("AND l.dpd >= 90 ");
                    if (rule.getMinInactivityDays() != null && rule.getMinInactivityDays() > 0) {
                        sql.append("AND c.created_at <= NOW() - INTERVAL '").append(rule.getMinInactivityDays()).append(" days' ");
                    }
                    break;
                case CUSTOM:
                    // Custom rules can be extended based on criteria JSON
                    break;
            }

            // Apply bucket filter
            if (rule.getIncludeBuckets() != null && !rule.getIncludeBuckets().isBlank()) {
                String[] buckets = rule.getIncludeBuckets().split(",");
                sql.append("AND l.bucket IN (");
                for (int i = 0; i < buckets.length; i++) {
                    sql.append("'").append(buckets[i].trim()).append("'");
                    if (i < buckets.length - 1) sql.append(",");
                }
                sql.append(") ");
            }

            // Apply status exclusion filter
            if (rule.getExcludeStatuses() != null && !rule.getExcludeStatuses().isBlank()) {
                String[] statuses = rule.getExcludeStatuses().split(",");
                sql.append("AND c.case_status NOT IN (");
                for (int i = 0; i < statuses.length; i++) {
                    sql.append("'").append(statuses[i].trim()).append("'");
                    if (i < statuses.length - 1) sql.append(",");
                }
                sql.append(") ");
            }

            // Exclude already closed cases in cycle_closure_cases
            sql.append("AND NOT EXISTS (SELECT 1 FROM cycle_closure_cases cc WHERE cc.case_id = c.id AND cc.closure_status = 'COMPLETED') ");

            sql.append("ORDER BY l.outstanding_amount ASC, l.dpd DESC ");
            sql.append("LIMIT 1000"); // Limit for safety

            log.debug("Executing eligible cases query: {}", sql);

            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> EligibleCaseData.builder()
                    .id(rs.getLong("id"))
                    .caseNumber(rs.getString("case_number"))
                    .loanAccountNumber(rs.getString("loan_account_number"))
                    .customerName(rs.getString("customer_name"))
                    .outstandingAmount(rs.getBigDecimal("outstanding_amount"))
                    .dpd(rs.getInt("dpd"))
                    .bucket(rs.getString("bucket"))
                    .caseStatus(rs.getString("case_status"))
                    .lastActivityDate(rs.getTimestamp("last_activity_date") != null ?
                            rs.getTimestamp("last_activity_date").toLocalDateTime() : null)
                    .zeroOutstandingSince(rs.getTimestamp("zero_outstanding_since") != null ?
                            rs.getTimestamp("zero_outstanding_since").toLocalDateTime() : null)
                    .build());

        } catch (Exception e) {
            log.error("Failed to fetch eligible cases: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Close a case by updating the cases table directly
     */
    private boolean closeCaseInDatabase(Long caseId, String closureReason, Long userId) {
        try {
            String sql = "UPDATE cases SET status = 400, case_status = 'CLOSED', " +
                    "case_closed_at = NOW(), case_closure_reason = ?, updated_at = NOW(), updated_by = ? " +
                    "WHERE id = ? AND status = 200";
            int updated = jdbcTemplate.update(sql, closureReason, userId, caseId);
            return updated > 0;
        } catch (Exception e) {
            log.error("Failed to close case {} in database: {}", caseId, e.getMessage());
            return false;
        }
    }

    private String generateRuleCode(ClosureRuleType ruleType) {
        String prefix = switch (ruleType) {
            case ZERO_OUTSTANDING -> "ZO";
            case FULLY_SETTLED -> "FS";
            case WRITTEN_OFF -> "WO";
            case NO_ACTIVITY -> "NA";
            case NPA_AGED -> "NPA";
            case CUSTOM -> "CUS";
        };
        return prefix + "-" + System.currentTimeMillis() % 100000;
    }

    private ClosureRuleDTO mapToDTO(ClosureRule rule) {
        return ClosureRuleDTO.builder()
                .id(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .cronExpression(rule.getCronExpression())
                .isScheduled(rule.getIsScheduled())
                .closureReason(rule.getClosureReason())
                .minZeroOutstandingDays(rule.getMinZeroOutstandingDays())
                .minInactivityDays(rule.getMinInactivityDays())
                .includeBuckets(rule.getIncludeBuckets())
                .excludeStatuses(rule.getExcludeStatuses())
                .isActive(rule.getIsActive())
                .priority(rule.getPriority())
                .lastExecutedAt(rule.getLastExecutedAt())
                .lastExecutionCount(rule.getLastExecutionCount())
                .totalCasesClosed(rule.getTotalCasesClosed())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .createdBy(rule.getCreatedBy())
                .updatedBy(rule.getUpdatedBy())
                .nextScheduledRun(rule.getCronExpression() != null ? getNextScheduledRun(rule.getCronExpression()) : null)
                .build();
    }

    private RuleExecutionDTO mapToExecutionDTO(ClosureRuleExecution execution) {
        return RuleExecutionDTO.builder()
                .id(execution.getId())
                .executionId(execution.getExecutionId())
                .ruleId(execution.getRule() != null ? execution.getRule().getId() : null)
                .ruleCode(execution.getRule() != null ? execution.getRule().getRuleCode() : null)
                .ruleName(execution.getRule() != null ? execution.getRule().getRuleName() : null)
                .status(execution.getStatus())
                .isSimulation(execution.getIsSimulation())
                .totalEligible(execution.getTotalEligible())
                .totalProcessed(execution.getTotalProcessed())
                .totalSuccess(execution.getTotalSuccess())
                .totalFailed(execution.getTotalFailed())
                .totalSkipped(execution.getTotalSkipped())
                .errorMessage(execution.getErrorMessage())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .durationMs(execution.getDurationMs())
                .triggeredBy(execution.getTriggeredBy())
                .executedBy(execution.getExecutedBy())
                .createdAt(execution.getCreatedAt())
                .build();
    }

    private SimulationResultDTO.EligibleCaseDTO mapToEligibleCaseDTO(EligibleCaseData caseData) {
        Integer daysSinceLastActivity = null;
        if (caseData.getLastActivityDate() != null) {
            daysSinceLastActivity = (int) ChronoUnit.DAYS.between(caseData.getLastActivityDate(), LocalDateTime.now());
        }

        Integer daysWithZeroOutstanding = null;
        if (caseData.getZeroOutstandingSince() != null) {
            daysWithZeroOutstanding = (int) ChronoUnit.DAYS.between(caseData.getZeroOutstandingSince(), LocalDateTime.now());
        }

        return SimulationResultDTO.EligibleCaseDTO.builder()
                .caseId(caseData.getId())
                .caseNumber(caseData.getCaseNumber())
                .loanAccountNumber(caseData.getLoanAccountNumber())
                .customerName(caseData.getCustomerName())
                .outstandingAmount(caseData.getOutstandingAmount())
                .dpd(caseData.getDpd())
                .bucket(caseData.getBucket())
                .currentStatus(caseData.getCaseStatus())
                .lastActivityDate(caseData.getLastActivityDate() != null ?
                        caseData.getLastActivityDate().format(DATE_FORMATTER) : null)
                .daysSinceLastActivity(daysSinceLastActivity)
                .daysWithZeroOutstanding(daysWithZeroOutstanding)
                .build();
    }
}

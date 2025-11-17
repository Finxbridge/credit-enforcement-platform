package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.DashboardResponse;
import com.finx.strategyengineservice.domain.dto.DashboardSummary;
import com.finx.strategyengineservice.domain.dto.StrategyDashboardItem;
import com.finx.strategyengineservice.domain.dto.StrategyRequest;
import com.finx.strategyengineservice.domain.dto.StrategyResponse;
import com.finx.strategyengineservice.domain.entity.*;
import com.finx.strategyengineservice.domain.enums.ActionType;
import com.finx.strategyengineservice.domain.enums.RuleOperator;
import com.finx.strategyengineservice.domain.enums.ScheduleType;
import com.finx.strategyengineservice.domain.enums.StrategyStatus;
import com.finx.strategyengineservice.exception.BusinessException;
import com.finx.strategyengineservice.exception.ResourceNotFoundException;
import com.finx.strategyengineservice.repository.*;
import com.finx.strategyengineservice.service.CaseFilterService;
import com.finx.strategyengineservice.service.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Strategy Service
 * Creates complete strategy with all configurations in a single transaction
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StrategyServiceImpl implements StrategyService {

    private final StrategyRepository strategyRepository;
    private final StrategyRuleRepository ruleRepository;
    private final StrategyActionRepository actionRepository;
    private final ScheduledJobRepository scheduledJobRepository;
    private final CaseFilterService caseFilterService;

    private static final String SERVICE_NAME = "strategy-engine-service";
    private static final String JOB_TYPE = "STRATEGY_EXECUTION";
    private static final String REFERENCE_TYPE = "STRATEGY";

    @SuppressWarnings("null")
    @Override
    public StrategyResponse createStrategy(StrategyRequest request) {
        log.info("Creating unified strategy: {}", request.getRuleName());

        // 1. Create Strategy entity
        Strategy strategy = createStrategyEntity(request);
        strategy = strategyRepository.save(strategy);

        // 2. Create Strategy Rules from filters
        List<StrategyRule> rules = createRulesFromFilters(strategy.getId(), request.getFilterConfig());
        ruleRepository.saveAll(rules);

        // 3. Create Strategy Action from template
        StrategyAction action = createActionFromTemplate(strategy.getId(), request.getTemplateConfig());
        actionRepository.save(action);

        // 4. Create Scheduled Job for automation
        ScheduledJob scheduledJob = createScheduledJob(strategy, request.getScheduleConfig());
        scheduledJobRepository.save(scheduledJob);

        log.info("Strategy created successfully: ID={}, Rules={}, Actions=1",
                strategy.getId(), rules.size());

        // 5. Build and return response
        return buildResponse(strategy, rules, action, scheduledJob, null);
    }

    @SuppressWarnings("null")
    @Override
    public StrategyResponse updateStrategy(Long strategyId, StrategyRequest request) {
        log.info("Updating unified strategy: ID={}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        // Update strategy entity
        updateStrategyEntity(strategy, request);
        final Strategy savedStrategy = strategyRepository.save(strategy);

        // Delete old rules and create new ones
        ruleRepository.deleteByStrategyId(strategyId);
        List<StrategyRule> rules = createRulesFromFilters(strategyId, request.getFilterConfig());
        ruleRepository.saveAll(rules);

        // Delete old actions and create new one
        actionRepository.deleteByStrategyId(strategyId);
        StrategyAction action = createActionFromTemplate(strategyId, request.getTemplateConfig());
        actionRepository.save(action);

        // Update scheduled job
        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElseGet(() -> createScheduledJob(savedStrategy, request.getScheduleConfig()));

        updateScheduledJob(scheduledJob, savedStrategy, request.getScheduleConfig());
        scheduledJobRepository.save(scheduledJob);

        log.info("Strategy updated successfully: ID={}", strategyId);

        return buildResponse(savedStrategy, rules, action, scheduledJob, null);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public StrategyResponse getStrategy(Long strategyId) {
        log.info("Fetching unified strategy: ID={}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        List<StrategyRule> rules = ruleRepository.findByStrategyIdOrderByRuleOrderAsc(strategyId);
        StrategyAction action = actionRepository.findByStrategyIdOrderByActionOrderAsc(strategyId)
                .stream().findFirst().orElse(null);
        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElse(null);

        return buildResponse(strategy, rules, action, scheduledJob, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrategyResponse> getAllStrategies(String status) {
        log.info("Fetching all strategies with status: {}", status);

        List<Strategy> strategies;
        if (status != null && !status.isBlank()) {
            StrategyStatus statusEnum = StrategyStatus.valueOf(status);
            strategies = strategyRepository.findByStatus(statusEnum);
        } else {
            strategies = strategyRepository.findAll();
        }

        return strategies.stream()
                .map(strategy -> {
                    List<StrategyRule> rules = ruleRepository.findByStrategyIdOrderByRuleOrderAsc(strategy.getId());
                    StrategyAction action = actionRepository.findByStrategyIdOrderByActionOrderAsc(strategy.getId())
                            .stream().findFirst().orElse(null);
                    ScheduledJob scheduledJob = scheduledJobRepository
                            .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategy.getId())
                            .orElse(null);
                    return buildResponse(strategy, rules, action, scheduledJob, null);
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void deleteStrategy(Long strategyId) {
        log.info("Deleting unified strategy: ID={}", strategyId);

        if (!strategyRepository.existsById(strategyId)) {
            throw new ResourceNotFoundException("Strategy not found: " + strategyId);
        }

        // Delete rules
        ruleRepository.deleteByStrategyId(strategyId);

        // Delete actions
        actionRepository.deleteByStrategyId(strategyId);

        // Delete scheduled job
        scheduledJobRepository.findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .ifPresent(scheduledJobRepository::delete);

        // Delete strategy
        strategyRepository.deleteById(strategyId);

        log.info("Strategy deleted successfully: ID={}", strategyId);
    }

    @SuppressWarnings("null")
    @Override
    public StrategyResponse updateStrategyStatus(Long strategyId, String status) {
        log.info("Updating strategy status: ID={}, Status={}", strategyId, status);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        StrategyStatus statusEnum = StrategyStatus.valueOf(status);
        strategy.setStatus(statusEnum);
        strategy.setIsActive(StrategyStatus.ACTIVE.equals(statusEnum));
        strategy.setUpdatedAt(LocalDateTime.now());
        strategyRepository.save(strategy);

        return getStrategy(strategyId);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public StrategyResponse simulateStrategy(Long strategyId) {
        log.info("Simulating unified strategy: ID={}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        List<StrategyRule> rules = ruleRepository.findByStrategyIdOrderByRuleOrderAsc(strategyId);

        // Filter cases to get count
        List<Case> matchedCases = rules.isEmpty() ? Collections.emptyList()
                : caseFilterService.filterCasesByRules(rules);

        int estimatedCount = matchedCases.size();
        log.info("Strategy simulation complete: {} cases matched", estimatedCount);

        StrategyAction action = actionRepository.findByStrategyIdOrderByActionOrderAsc(strategyId)
                .stream().findFirst().orElse(null);
        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElse(null);

        return buildResponse(strategy, rules, action, scheduledJob, estimatedCount);
    }

    @SuppressWarnings("null")
    @Override
    public StrategyResponse toggleScheduler(Long strategyId, Boolean enabled) {
        log.info("Toggling scheduler for strategy: ID={}, Enabled={}", strategyId, enabled);

        strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElseThrow(() -> new BusinessException("Scheduled job not found for strategy: " + strategyId));

        scheduledJob.setIsEnabled(enabled);
        scheduledJob.setUpdatedAt(LocalDateTime.now());

        if (enabled) {
            // Calculate next run time when enabling
            LocalDateTime nextRun = calculateNextRunTime(scheduledJob);
            scheduledJob.setNextRunAt(nextRun);
        } else {
            scheduledJob.setNextRunAt(null);
        }

        scheduledJobRepository.save(scheduledJob);

        return getStrategy(strategyId);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardMetrics() {
        log.info("Fetching dashboard metrics");

        // Get all strategies
        List<Strategy> allStrategies = strategyRepository.findAll();

        // Calculate summary statistics
        int totalStrategies = allStrategies.size();
        int activeStrategies = (int) allStrategies.stream().filter(s -> StrategyStatus.ACTIVE.equals(s.getStatus()))
                .count();
        int inactiveStrategies = (int) allStrategies.stream().filter(s -> StrategyStatus.INACTIVE.equals(s.getStatus()))
                .count();
        int draftStrategies = (int) allStrategies.stream().filter(s -> StrategyStatus.DRAFT.equals(s.getStatus()))
                .count();

        // Calculate total executions and overall success rate
        long totalExecutions = allStrategies.stream()
                .mapToLong(s -> (long) s.getSuccessCount() + s.getFailureCount())
                .sum();

        long totalSuccess = allStrategies.stream()
                .mapToLong(s -> (long) s.getSuccessCount())
                .sum();

        double overallSuccessRate = totalExecutions > 0
                ? (totalSuccess * 100.0 / totalExecutions)
                : 0.0;

        // Count enabled schedulers
        Long enabledSchedulers = scheduledJobRepository
                .countByServiceNameAndIsEnabled(SERVICE_NAME, true);

        // Build summary
        DashboardSummary summary = DashboardSummary.builder()
                .totalStrategies(totalStrategies)
                .activeStrategies(activeStrategies)
                .inactiveStrategies(inactiveStrategies)
                .draftStrategies(draftStrategies)
                .totalExecutions(totalExecutions)
                .overallSuccessRate(Math.round(overallSuccessRate * 100.0) / 100.0)
                .enabledSchedulers(enabledSchedulers.intValue())
                .build();

        // Build strategy dashboard items
        List<StrategyDashboardItem> strategyItems = allStrategies.stream()
                .map(this::buildDashboardItem)
                .collect(Collectors.toList());

        log.info("Dashboard metrics fetched: {} strategies, {} enabled schedulers",
                totalStrategies, enabledSchedulers);

        return DashboardResponse.builder()
                .summary(summary)
                .strategies(strategyItems)
                .build();
    }

    // ===================================
    // PRIVATE HELPER METHODS
    // ===================================

    private Strategy createStrategyEntity(StrategyRequest request) {
        String strategyCode = generateStrategyCode(request.getRuleName());
        StrategyStatus statusEnum = StrategyStatus.valueOf(request.getStatus());

        return Strategy.builder()
                .strategyCode(strategyCode)
                .strategyName(request.getRuleName())
                .strategyType("COLLECTION") // Default type
                .description(request.getDescription())
                .status(statusEnum)
                .isActive(StrategyStatus.ACTIVE.equals(statusEnum))
                .priority(request.getPriority())
                .triggerFrequency(request.getScheduleConfig().getFrequency())
                .triggerTime(parseTime(request.getScheduleConfig()))
                .triggerDays(parseDays(request.getScheduleConfig()))
                .successCount(0)
                .failureCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void updateStrategyEntity(Strategy strategy, StrategyRequest request) {
        StrategyStatus statusEnum = StrategyStatus.valueOf(request.getStatus());
        strategy.setStrategyName(request.getRuleName());
        strategy.setDescription(request.getDescription());
        strategy.setStatus(statusEnum);
        strategy.setIsActive(StrategyStatus.ACTIVE.equals(statusEnum));
        strategy.setPriority(request.getPriority());
        strategy.setTriggerFrequency(request.getScheduleConfig().getFrequency());
        strategy.setTriggerTime(parseTime(request.getScheduleConfig()));
        strategy.setTriggerDays(parseDays(request.getScheduleConfig()));
        strategy.setUpdatedAt(LocalDateTime.now());
    }

    @SuppressWarnings("null")
    private List<StrategyRule> createRulesFromFilters(Long strategyId, StrategyRequest.FilterConfig filters) {
        List<StrategyRule> rules = new ArrayList<>();
        int ruleOrder = 0;

        // Get strategy reference
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        // DPD Filter (required)
        rules.add(createDpdRule(strategy, filters.getDpd(), ruleOrder++));

        // Outstanding Principal
        if (filters.getOutstandingPrincipal() != null) {
            rules.add(createNumericRule(strategy, "loan.total_outstanding",
                    filters.getOutstandingPrincipal(), ruleOrder++));
        }

        // Payment Amount (if needed in loan_details table)
        if (filters.getPaymentAmount() != null) {
            rules.add(createNumericRule(strategy, "loan.emi_amount",
                    filters.getPaymentAmount(), ruleOrder++));
        }

        // Text Filters
        if (filters.getLanguages() != null && !filters.getLanguages().isEmpty()) {
            rules.add(createTextRule(strategy, "case.language", filters.getLanguages(), ruleOrder++));
        }

        if (filters.getProducts() != null && !filters.getProducts().isEmpty()) {
            rules.add(createTextRule(strategy, "loan.product_code", filters.getProducts(), ruleOrder++));
        }

        if (filters.getPincodes() != null && !filters.getPincodes().isEmpty()) {
            rules.add(createTextRule(strategy, "customer.pincode", filters.getPincodes(), ruleOrder++));
        }

        if (filters.getStates() != null && !filters.getStates().isEmpty()) {
            rules.add(createTextRule(strategy, "customer.state", filters.getStates(), ruleOrder++));
        }

        if (filters.getBuckets() != null && !filters.getBuckets().isEmpty()) {
            rules.add(createTextRule(strategy, "loan.bucket", filters.getBuckets(), ruleOrder++));
        }

        return rules;
    }

    private StrategyRule createDpdRule(Strategy strategy, StrategyRequest.DpdFilter dpd, int order) {
        String operator = dpd.getOperator();
        String value;

        if ("BETWEEN".equals(operator)) {
            value = dpd.getMinDpd() + "," + dpd.getMaxDpd();
        } else {
            value = String.valueOf(dpd.getValue());
        }

        RuleOperator operatorEnum = RuleOperator.valueOf(operator);

        return StrategyRule.builder()
                .strategy(strategy)
                .ruleName("DPD Filter")
                .ruleOrder(order)
                .fieldName("loan.dpd")
                .operator(operatorEnum)
                .fieldValue(value)
                .logicalOperator("AND")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private StrategyRule createNumericRule(Strategy strategy, String fieldName,
            StrategyRequest.NumericFilter filter, int order) {
        String value;
        if ("BETWEEN".equals(filter.getOperator())) {
            value = filter.getMinValue() + "," + filter.getMaxValue();
        } else {
            value = String.valueOf(filter.getValue());
        }

        RuleOperator operatorEnum = RuleOperator.valueOf(filter.getOperator());

        return StrategyRule.builder()
                .strategy(strategy)
                .ruleName(fieldName + " Filter")
                .ruleOrder(order)
                .fieldName(fieldName)
                .operator(operatorEnum)
                .fieldValue(value)
                .logicalOperator("AND")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private StrategyRule createTextRule(Strategy strategy, String fieldName, List<String> values, int order) {
        String value = String.join(",", values);

        return StrategyRule.builder()
                .strategy(strategy)
                .ruleName(fieldName + " Filter")
                .ruleOrder(order)
                .fieldName(fieldName)
                .operator(RuleOperator.IN)
                .fieldValue(value)
                .logicalOperator("AND")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @SuppressWarnings("null")
    private StrategyAction createActionFromTemplate(Long strategyId,
            StrategyRequest.TemplateConfig template) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));
        ActionType actionType = mapTemplateTypeToActionType(template.getTemplateType());

        return StrategyAction.builder()
                .strategy(strategy)
                .actionType(actionType)
                .actionOrder(0)
                .templateId(template.getTemplateId())
                .channel(template.getTemplateType())
                .priority(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ActionType mapTemplateTypeToActionType(String templateType) {
        return switch (templateType) {
            case "SMS" -> ActionType.SEND_SMS;
            case "WHATSAPP" -> ActionType.SEND_WHATSAPP;
            case "EMAIL" -> ActionType.SEND_EMAIL;
            case "IVR" -> ActionType.SCHEDULE_CALL;
            case "NOTICE" -> ActionType.CREATE_NOTICE;
            default -> throw new BusinessException("Unsupported template type: " + templateType);
        };
    }

    private ScheduledJob createScheduledJob(Strategy strategy, StrategyRequest.ScheduleConfig schedule) {
        ScheduledJob job = new ScheduledJob();
        job.setServiceName(SERVICE_NAME);
        job.setJobName(strategy.getStrategyName());
        job.setJobType(JOB_TYPE);
        job.setJobReferenceId(strategy.getId());
        job.setJobReferenceType(REFERENCE_TYPE);
        job.setIsEnabled(false); // Disabled by default

        updateScheduleConfig(job, schedule);

        return job;
    }

    private void updateScheduledJob(ScheduledJob job, Strategy strategy,
            StrategyRequest.ScheduleConfig schedule) {
        job.setJobName(strategy.getStrategyName());
        updateScheduleConfig(job, schedule);
        job.setUpdatedAt(LocalDateTime.now());
    }

    private void updateScheduleConfig(ScheduledJob job, StrategyRequest.ScheduleConfig schedule) {
        ScheduleType scheduleType = switch (schedule.getFrequency()) {
            case "DAILY" -> ScheduleType.DAILY;
            case "WEEKLY" -> ScheduleType.WEEKLY;
            case "EVENT_BASED" -> ScheduleType.EVENT_BASED;
            default -> ScheduleType.DAILY;
        };

        job.setScheduleType(scheduleType);
        job.setTimezone(schedule.getTimezone());

        if ("DAILY".equals(schedule.getFrequency())) {
            job.setScheduleTime(LocalTime.parse(schedule.getDailyTime()));
        } else if ("WEEKLY".equals(schedule.getFrequency())) {
            job.setScheduleTime(LocalTime.parse(schedule.getWeeklyTime()));
            job.setScheduleDays(String.join(",", schedule.getWeeklyDays()));
        }
    }

    private LocalDateTime calculateNextRunTime(ScheduledJob job) {
        // Simplified - full implementation in StrategySchedulerServiceImpl
        LocalDateTime now = LocalDateTime.now();
        if (job.getScheduleTime() != null) {
            return LocalDateTime.of(now.toLocalDate().plusDays(1), job.getScheduleTime());
        }
        return now.plusDays(1);
    }

    private String generateStrategyCode(String ruleName) {
        String code = ruleName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_");
        return code + "_" + System.currentTimeMillis();
    }

    private LocalTime parseTime(StrategyRequest.ScheduleConfig schedule) {
        if ("DAILY".equals(schedule.getFrequency()) && schedule.getDailyTime() != null) {
            return LocalTime.parse(schedule.getDailyTime());
        } else if ("WEEKLY".equals(schedule.getFrequency()) && schedule.getWeeklyTime() != null) {
            return LocalTime.parse(schedule.getWeeklyTime());
        }
        return null;
    }

    private String parseDays(StrategyRequest.ScheduleConfig schedule) {
        if ("WEEKLY".equals(schedule.getFrequency()) && schedule.getWeeklyDays() != null) {
            return String.join(",", schedule.getWeeklyDays());
        }
        return null;
    }

    private StrategyResponse buildResponse(Strategy strategy, List<StrategyRule> rules,
            StrategyAction action, ScheduledJob scheduledJob,
            Integer estimatedCases) {
        return StrategyResponse.builder()
                .strategyId(strategy.getId())
                .strategyCode(strategy.getStrategyCode())
                .ruleName(strategy.getStrategyName())
                .status(strategy.getStatus() != null ? strategy.getStatus().name() : null)
                .priority(strategy.getPriority())
                .description(strategy.getDescription())
                .template(buildTemplateInfo(action))
                .filters(buildFilterSummary(rules, estimatedCases))
                .schedule(buildScheduleInfo(scheduledJob, strategy))
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .lastRunAt(strategy.getLastRunAt())
                .successCount(strategy.getSuccessCount())
                .failureCount(strategy.getFailureCount())
                .build();
    }

    private StrategyResponse.TemplateInfo buildTemplateInfo(StrategyAction action) {
        if (action == null)
            return null;

        return StrategyResponse.TemplateInfo.builder()
                .templateType(action.getChannel())
                .templateId(action.getTemplateId())
                .templateName(action.getActionType().name())
                .build();
    }

    private StrategyResponse.FilterSummary buildFilterSummary(List<StrategyRule> rules,
            Integer estimatedCases) {
        StrategyResponse.FilterSummary summary = new StrategyResponse.FilterSummary();
        summary.setEstimatedCasesMatched(estimatedCases);

        for (StrategyRule rule : rules) {
            String humanReadable = rule.getOperator() + " " + rule.getFieldValue();

            if ("loan.dpd".equals(rule.getFieldName())) {
                summary.setDpd(humanReadable);
            } else if ("loan.total_outstanding".equals(rule.getFieldName())) {
                summary.setOutstandingPrincipal(humanReadable);
            } else if ("loan.emi_amount".equals(rule.getFieldName())) {
                summary.setPaymentAmount(humanReadable);
            } else if ("loan.bucket".equals(rule.getFieldName())) {
                summary.setBuckets(Arrays.asList(rule.getFieldValue().split(",")));
            }
            // Add more mappings as needed
        }

        return summary;
    }

    private StrategyResponse.ScheduleInfo buildScheduleInfo(ScheduledJob job, Strategy strategy) {
        if (job == null)
            return null;

        String scheduleText = buildScheduleText(job);

        return StrategyResponse.ScheduleInfo.builder()
                .frequency(strategy.getTriggerFrequency())
                .schedule(scheduleText)
                .nextRunAt(job.getNextRunAt())
                .isEnabled(job.getIsEnabled())
                .build();
    }

    private String buildScheduleText(ScheduledJob job) {
        if (job.getScheduleType() == ScheduleType.DAILY) {
            return "Daily at " + job.getScheduleTime();
        } else if (job.getScheduleType() == ScheduleType.WEEKLY) {
            return "Weekly on " + job.getScheduleDays() + " at " + job.getScheduleTime();
        }
        return "Event Based";
    }

    /**
     * Build dashboard item for a single strategy
     */
    private StrategyDashboardItem buildDashboardItem(Strategy strategy) {
        // Get scheduled job for this strategy
        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategy.getId())
                .orElse(null);

        // Get action to determine channel
        StrategyAction action = actionRepository.findByStrategyIdOrderByActionOrderAsc(strategy.getId())
                .stream().findFirst().orElse(null);

        // Calculate success rate
        int totalExecutions = strategy.getSuccessCount() + strategy.getFailureCount();
        double successRate = totalExecutions > 0
                ? (strategy.getSuccessCount() * 100.0 / totalExecutions)
                : 0.0;

        return StrategyDashboardItem.builder()
                .strategyId(strategy.getId())
                .strategyName(strategy.getStrategyName())
                .lastRun(strategy.getLastRunAt())
                .nextRun(scheduledJob != null ? scheduledJob.getNextRunAt() : null)
                .channel(action != null ? action.getChannel() : null)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .totalExecutions(totalExecutions)
                .successCount(strategy.getSuccessCount())
                .failureCount(strategy.getFailureCount())
                .status(strategy.getStatus().name())
                .isSchedulerEnabled(scheduledJob != null && scheduledJob.getIsEnabled())
                .priority(strategy.getPriority())
                .frequency(strategy.getTriggerFrequency())
                .build();
    }
}

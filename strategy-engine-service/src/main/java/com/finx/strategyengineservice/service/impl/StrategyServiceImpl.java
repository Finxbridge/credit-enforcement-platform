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
        log.info("Creating unified strategy: {}", request.getStrategyName());

        // 1. Create Strategy entity
        Strategy strategy = createStrategyEntity(request);
        strategy = strategyRepository.save(strategy);

        // 2. Create Strategy Rules from filters
        List<StrategyRule> rules = createRulesFromFilters(strategy.getId(), request.getFilters());
        ruleRepository.saveAll(rules);

        // 3. Create Strategy Action from channel/template
        StrategyAction action = createActionFromChannel(strategy.getId(), request.getChannel());
        actionRepository.save(action);

        // 4. Create Scheduled Job for automation
        ScheduledJob scheduledJob = createScheduledJob(strategy, request.getSchedule());
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
        List<StrategyRule> rules = createRulesFromFilters(strategyId, request.getFilters());
        ruleRepository.saveAll(rules);

        // Delete old actions and create new one
        actionRepository.deleteByStrategyId(strategyId);
        StrategyAction action = createActionFromChannel(strategyId, request.getChannel());
        actionRepository.save(action);

        // Update scheduled job
        ScheduledJob scheduledJob = scheduledJobRepository
                .findByJobReferenceTypeAndJobReferenceId(REFERENCE_TYPE, strategyId)
                .orElseGet(() -> createScheduledJob(savedStrategy, request.getSchedule()));

        updateScheduledJob(scheduledJob, savedStrategy, request.getSchedule());
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
        String strategyCode = generateStrategyCode(request.getStrategyName());
        StrategyStatus statusEnum = StrategyStatus.valueOf(request.getStatus());

        return Strategy.builder()
                .strategyCode(strategyCode)
                .strategyName(request.getStrategyName())
                .strategyType("COLLECTION") // Default type
                .description(request.getDescription())
                .status(statusEnum)
                .isActive(StrategyStatus.ACTIVE.equals(statusEnum))
                .priority(request.getPriority())
                .triggerFrequency(request.getSchedule().getFrequency())
                .triggerTime(parseTime(request.getSchedule()))
                .triggerDays(parseDays(request.getSchedule()))
                .successCount(0)
                .failureCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void updateStrategyEntity(Strategy strategy, StrategyRequest request) {
        StrategyStatus statusEnum = StrategyStatus.valueOf(request.getStatus());
        strategy.setStrategyName(request.getStrategyName());
        strategy.setDescription(request.getDescription());
        strategy.setStatus(statusEnum);
        strategy.setIsActive(StrategyStatus.ACTIVE.equals(statusEnum));
        strategy.setPriority(request.getPriority());
        strategy.setTriggerFrequency(request.getSchedule().getFrequency());
        strategy.setTriggerTime(parseTime(request.getSchedule()));
        strategy.setTriggerDays(parseDays(request.getSchedule()));
        strategy.setUpdatedAt(LocalDateTime.now());
    }

    @SuppressWarnings("null")
    private List<StrategyRule> createRulesFromFilters(Long strategyId, StrategyRequest.Filters filters) {
        List<StrategyRule> rules = new ArrayList<>();
        int ruleOrder = 0;

        // Get strategy reference
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));

        // DPD Range Filter (required)
        rules.add(createDpdRangeRule(strategy, filters.getDpdRange(), ruleOrder++));

        // Outstanding Amount Filter (simplified)
        if (filters.getOutstandingAmount() != null) {
            // Simple filter: >= outstandingAmount
            rules.add(createSimpleNumericRule(strategy, "loan.total_outstanding",
                    filters.getOutstandingAmount(), "GREATER_THAN_OR_EQUAL", ruleOrder++));
        } else if (filters.getOutstandingRange() != null) {
            // Range filter: BETWEEN from and to
            rules.add(createRangeNumericRule(strategy, "loan.total_outstanding",
                    filters.getOutstandingRange().getFrom(), filters.getOutstandingRange().getTo(), ruleOrder++));
        }

        // Text Filters (note: singular names now)
        if (filters.getLanguage() != null && !filters.getLanguage().isEmpty()) {
            rules.add(createTextRule(strategy, "case.language", filters.getLanguage(), ruleOrder++));
        }

        if (filters.getProduct() != null && !filters.getProduct().isEmpty()) {
            rules.add(createTextRule(strategy, "loan.product_code", filters.getProduct(), ruleOrder++));
        }

        if (filters.getPincode() != null && !filters.getPincode().isEmpty()) {
            rules.add(createTextRule(strategy, "customer.pincode", filters.getPincode(), ruleOrder++));
        }

        if (filters.getState() != null && !filters.getState().isEmpty()) {
            rules.add(createTextRule(strategy, "customer.state", filters.getState(), ruleOrder++));
        }

        if (filters.getBucket() != null && !filters.getBucket().isEmpty()) {
            rules.add(createTextRule(strategy, "loan.bucket", filters.getBucket(), ruleOrder++));
        }

        return rules;
    }

    private StrategyRule createDpdRangeRule(Strategy strategy, StrategyRequest.DpdRange dpdRange, int order) {
        String value = dpdRange.getFrom() + "," + dpdRange.getTo();

        return StrategyRule.builder()
                .strategy(strategy)
                .ruleName("DPD Range Filter")
                .ruleOrder(order)
                .fieldName("loan.dpd")
                .operator(RuleOperator.BETWEEN)
                .fieldValue(value)
                .logicalOperator("AND")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private StrategyRule createSimpleNumericRule(Strategy strategy, String fieldName,
            Double value, String operator, int order) {
        RuleOperator operatorEnum = RuleOperator.valueOf(operator);

        return StrategyRule.builder()
                .strategy(strategy)
                .ruleName(fieldName + " Filter")
                .ruleOrder(order)
                .fieldName(fieldName)
                .operator(operatorEnum)
                .fieldValue(String.valueOf(value))
                .logicalOperator("AND")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private StrategyRule createRangeNumericRule(Strategy strategy, String fieldName,
            Double fromValue, Double toValue, int order) {
        String value = fromValue + "," + toValue;

        return StrategyRule.builder()
                .strategy(strategy)
                .ruleName(fieldName + " Range Filter")
                .ruleOrder(order)
                .fieldName(fieldName)
                .operator(RuleOperator.BETWEEN)
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
    private StrategyAction createActionFromChannel(Long strategyId, StrategyRequest.Channel channel) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found: " + strategyId));
        ActionType actionType = mapChannelTypeToActionType(channel.getType());

        // Determine templateId - use provided or lookup from templateName
        Long templateId = channel.getTemplateId();
        if (templateId == null) {
            // TODO: Implement template lookup service to get templateId by templateName
            log.warn("Template ID not provided, templateName lookup not yet implemented: {}",
                     channel.getTemplateName());
            templateId = 0L; // Placeholder
        }

        return StrategyAction.builder()
                .strategy(strategy)
                .actionType(actionType)
                .actionOrder(0)
                .templateId(templateId)
                .channel(channel.getType())
                .priority(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ActionType mapChannelTypeToActionType(String channelType) {
        return switch (channelType) {
            case "SMS" -> ActionType.SEND_SMS;
            case "WHATSAPP" -> ActionType.SEND_WHATSAPP;
            case "EMAIL" -> ActionType.SEND_EMAIL;
            case "IVR" -> ActionType.SCHEDULE_CALL;
            case "NOTICE" -> ActionType.CREATE_NOTICE;
            default -> throw new BusinessException("Unsupported channel type: " + channelType);
        };
    }

    private ScheduledJob createScheduledJob(Strategy strategy, StrategyRequest.Schedule schedule) {
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
            StrategyRequest.Schedule schedule) {
        job.setJobName(strategy.getStrategyName());
        updateScheduleConfig(job, schedule);
        job.setUpdatedAt(LocalDateTime.now());
    }

    private void updateScheduleConfig(ScheduledJob job, StrategyRequest.Schedule schedule) {
        ScheduleType scheduleType = switch (schedule.getFrequency()) {
            case "DAILY" -> ScheduleType.DAILY;
            case "WEEKLY" -> ScheduleType.WEEKLY;
            case "MONTHLY" -> ScheduleType.MONTHLY;
            default -> ScheduleType.DAILY;
        };

        job.setScheduleType(scheduleType);
        job.setTimezone(schedule.getTimezone());

        // Unified time field for all frequencies
        if (schedule.getTime() != null) {
            job.setScheduleTime(LocalTime.parse(schedule.getTime()));
        }

        // Handle days based on frequency
        if ("WEEKLY".equals(schedule.getFrequency()) && schedule.getDays() != null) {
            job.setScheduleDays(String.join(",", schedule.getDays()));
        } else if ("DAILY".equals(schedule.getFrequency()) && schedule.getDays() != null) {
            // For DAILY with specific days (e.g., Mon-Fri only)
            if (!schedule.getDays().contains("DAILY")) {
                job.setScheduleDays(String.join(",", schedule.getDays()));
            }
        }

        // Handle day of month for MONTHLY frequency
        if ("MONTHLY".equals(schedule.getFrequency()) && schedule.getDayOfMonth() != null) {
            job.setScheduleDays(String.valueOf(schedule.getDayOfMonth()));
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

    private LocalTime parseTime(StrategyRequest.Schedule schedule) {
        if (schedule.getTime() != null) {
            return LocalTime.parse(schedule.getTime());
        }
        return null;
    }

    private String parseDays(StrategyRequest.Schedule schedule) {
        if ("WEEKLY".equals(schedule.getFrequency()) && schedule.getDays() != null) {
            return String.join(",", schedule.getDays());
        } else if ("DAILY".equals(schedule.getFrequency()) && schedule.getDays() != null
                && !schedule.getDays().contains("DAILY")) {
            return String.join(",", schedule.getDays());
        } else if ("MONTHLY".equals(schedule.getFrequency()) && schedule.getDayOfMonth() != null) {
            return String.valueOf(schedule.getDayOfMonth());
        }
        return null;
    }

    private StrategyResponse buildResponse(Strategy strategy, List<StrategyRule> rules,
            StrategyAction action, ScheduledJob scheduledJob,
            Integer estimatedCases) {
        return StrategyResponse.builder()
                .strategyId(strategy.getId())
                .strategyCode(strategy.getStrategyCode())
                .strategyName(strategy.getStrategyName())
                .status(strategy.getStatus() != null ? strategy.getStatus().name() : null)
                .priority(strategy.getPriority())
                .description(strategy.getDescription())
                .channel(buildChannelInfo(action))
                .filters(buildFiltersInfo(rules, estimatedCases))
                .schedule(buildScheduleInfo(scheduledJob, strategy))
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .lastRunAt(strategy.getLastRunAt())
                .successCount(strategy.getSuccessCount())
                .failureCount(strategy.getFailureCount())
                .build();
    }

    private StrategyResponse.Channel buildChannelInfo(StrategyAction action) {
        if (action == null)
            return null;

        return StrategyResponse.Channel.builder()
                .type(action.getChannel())
                .templateId(action.getTemplateId())
                .templateName(action.getActionType().name())
                .build();
    }

    private StrategyResponse.Filters buildFiltersInfo(List<StrategyRule> rules,
            Integer estimatedCases) {
        StrategyResponse.Filters filters = new StrategyResponse.Filters();
        filters.setEstimatedCasesMatched(estimatedCases);

        for (StrategyRule rule : rules) {
            String fieldName = rule.getFieldName();
            String fieldValue = rule.getFieldValue();
            RuleOperator operator = rule.getOperator();

            // DPD Range
            if ("loan.dpd".equals(fieldName)) {
                if (RuleOperator.BETWEEN.equals(operator)) {
                    String[] parts = fieldValue.split(",");
                    filters.setDpdRange(parts[0] + "-" + parts[1]);
                } else {
                    filters.setDpdRange(operator + " " + fieldValue);
                }
            }
            // Outstanding Amount
            else if ("loan.total_outstanding".equals(fieldName)) {
                if (RuleOperator.BETWEEN.equals(operator)) {
                    String[] parts = fieldValue.split(",");
                    filters.setOutstandingAmount(parts[0] + "-" + parts[1]);
                } else if (RuleOperator.GREATER_THAN_OR_EQUAL.equals(operator)) {
                    filters.setOutstandingAmount("≥ " + fieldValue);
                } else {
                    filters.setOutstandingAmount(operator + " " + fieldValue);
                }
            }
            // Text filters (singular names)
            else if ("case.language".equals(fieldName)) {
                filters.setLanguage(Arrays.asList(fieldValue.split(",")));
            } else if ("loan.product_code".equals(fieldName)) {
                filters.setProduct(Arrays.asList(fieldValue.split(",")));
            } else if ("customer.pincode".equals(fieldName)) {
                filters.setPincode(Arrays.asList(fieldValue.split(",")));
            } else if ("customer.state".equals(fieldName)) {
                filters.setState(Arrays.asList(fieldValue.split(",")));
            } else if ("loan.bucket".equals(fieldName)) {
                filters.setBucket(Arrays.asList(fieldValue.split(",")));
            }
        }

        return filters;
    }

    private StrategyResponse.Schedule buildScheduleInfo(ScheduledJob job, Strategy strategy) {
        if (job == null)
            return null;

        String scheduleText = buildScheduleText(job);

        // Parse days and day of month
        List<String> days = null;
        Integer dayOfMonth = null;

        if (job.getScheduleDays() != null && !job.getScheduleDays().isEmpty()) {
            if (ScheduleType.MONTHLY.equals(job.getScheduleType())) {
                try {
                    dayOfMonth = Integer.parseInt(job.getScheduleDays());
                } catch (NumberFormatException e) {
                    // Invalid format, ignore
                }
            } else {
                days = Arrays.asList(job.getScheduleDays().split(","));
            }
        }

        return StrategyResponse.Schedule.builder()
                .frequency(strategy.getTriggerFrequency())
                .time(job.getScheduleTime() != null ? job.getScheduleTime().toString() : null)
                .days(days)
                .dayOfMonth(dayOfMonth)
                .scheduleText(scheduleText)
                .nextRunAt(job.getNextRunAt())
                .isEnabled(job.getIsEnabled())
                .build();
    }

    private String buildScheduleText(ScheduledJob job) {
        if (job.getScheduleType() == ScheduleType.DAILY) {
            return "Daily at " + job.getScheduleTime();
        } else if (job.getScheduleType() == ScheduleType.WEEKLY) {
            return "Weekly on " + job.getScheduleDays() + " at " + job.getScheduleTime();
        } else if (job.getScheduleType() == ScheduleType.MONTHLY) {
            return "Monthly on day " + job.getScheduleDays() + " at " + job.getScheduleTime();
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

package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.*;
import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.entity.StrategyAction;
import com.finx.strategyengineservice.domain.entity.StrategyRule;
import com.finx.strategyengineservice.domain.enums.*;
import com.finx.strategyengineservice.exception.BusinessException;
import com.finx.strategyengineservice.repository.StrategyActionRepository;
import com.finx.strategyengineservice.repository.StrategyRepository;
import com.finx.strategyengineservice.repository.StrategyRuleRepository;
import com.finx.strategyengineservice.service.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StrategyServiceImpl implements StrategyService {

    private final StrategyRepository strategyRepository;
    private final StrategyRuleRepository strategyRuleRepository;
    private final StrategyActionRepository strategyActionRepository;

    @Override
    @Cacheable(value = "strategies", key = "'all'")
    @Transactional(readOnly = true)
    public List<StrategyDTO> getAllStrategies() {
        log.info("Fetching all strategies");
        List<Strategy> strategies = strategyRepository.findAll();
        return strategies.stream()
                .map(this::convertToStrategyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"strategies", "strategyDetails"}, allEntries = true)
    public StrategyDTO createStrategy(CreateStrategyRequest request) {
        log.info("Creating new strategy: {}", request.getName());

        // Validate strategy name uniqueness
        if (strategyRepository.existsByStrategyName(request.getName())) {
            throw new BusinessException("Strategy with name '" + request.getName() + "' already exists");
        }

        // Create strategy entity
        Strategy strategy = Strategy.builder()
                .strategyName(request.getName())
                .strategyType(request.getStrategyType() != null ? request.getStrategyType() : "COLLECTION")
                .description(request.getDescription())
                .triggerFrequency(request.getTriggerType())
                .scheduleExpression(request.getScheduleExpression())
                .eventType(request.getEventType())
                .status(request.getStatus() != null ? StrategyStatus.valueOf(request.getStatus()) : StrategyStatus.DRAFT)
                .priority(request.getPriority())
                .build();

        // Add rules
        if (request.getRules() != null) {
            request.getRules().forEach(ruleDTO -> {
                StrategyRule rule = convertToStrategyRule(ruleDTO);
                strategy.addRule(rule);
            });
        }

        // Add actions
        if (request.getActions() != null) {
            request.getActions().forEach(actionDTO -> {
                StrategyAction action = convertToStrategyAction(actionDTO);
                strategy.addAction(action);
            });
        }

        Strategy savedStrategy = strategyRepository.save(strategy);
        log.info("Strategy created successfully with ID: {}", savedStrategy.getId());

        return convertToStrategyDTO(savedStrategy);
    }

    @Override
    @Cacheable(value = "strategyDetails", key = "#strategyId")
    @Transactional(readOnly = true)
    public StrategyDetailDTO getStrategyById(Long strategyId) {
        log.info("Fetching strategy details for ID: {}", strategyId);
        Strategy strategy = strategyRepository.findByIdWithRulesAndActions(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        return convertToStrategyDetailDTO(strategy);
    }

    @Override
    @CacheEvict(value = {"strategies", "strategyDetails"}, allEntries = true)
    public StrategyDTO updateStrategy(Long strategyId, UpdateStrategyRequest request) {
        log.info("Updating strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        if (request.getName() != null) {
            if (!strategy.getStrategyName().equals(request.getName()) &&
                strategyRepository.existsByStrategyName(request.getName())) {
                throw new BusinessException("Strategy with name '" + request.getName() + "' already exists");
            }
            strategy.setStrategyName(request.getName());
        }

        if (request.getDescription() != null) {
            strategy.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            strategy.setStatus(StrategyStatus.valueOf(request.getStatus()));
        }

        if (request.getPriority() != null) {
            strategy.setPriority(request.getPriority());
        }

        Strategy updatedStrategy = strategyRepository.save(strategy);
        log.info("Strategy updated successfully: {}", strategyId);

        return convertToStrategyDTO(updatedStrategy);
    }

    @Override
    @CacheEvict(value = {"strategies", "strategyDetails", "strategyFilters", "strategyTemplate", "strategyTrigger"}, allEntries = true)
    public void deleteStrategy(Long strategyId) {
        log.info("Deleting strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        strategyRepository.delete(strategy);
        log.info("Strategy deleted successfully: {}", strategyId);
    }

    @Override
    @CacheEvict(value = {"strategyDetails", "strategyFilters"}, allEntries = true)
    public FiltersResponse updateFilters(Long strategyId, FiltersRequest request) {
        log.info("Updating filters for strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        // Remove existing rules
        strategyRuleRepository.deleteByStrategyId(strategyId);
        strategy.getRules().clear();

        // Add new rules
        request.getRules().forEach(ruleDTO -> {
            StrategyRule rule = convertToStrategyRule(ruleDTO);
            strategy.addRule(rule);
        });

        strategyRepository.save(strategy);
        log.info("Filters updated successfully for strategy: {}", strategyId);

        return getFilters(strategyId);
    }

    @Override
    @Cacheable(value = "strategyFilters", key = "#strategyId")
    @Transactional(readOnly = true)
    public FiltersResponse getFilters(Long strategyId) {
        log.info("Fetching filters for strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        List<StrategyRuleDTO> rules = strategy.getRules().stream()
                .map(this::convertToStrategyRuleDTO)
                .collect(Collectors.toList());

        return FiltersResponse.builder()
                .strategyId(strategyId)
                .filters(rules)
                .build();
    }

    @Override
    @CacheEvict(value = {"strategyDetails", "strategyTemplate"}, allEntries = true)
    public TemplateInfoResponse updateTemplate(Long strategyId, TemplateInfoRequest request) {
        log.info("Updating template for strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        // Update or create action with template info
        StrategyAction action = strategy.getActions().isEmpty()
            ? new StrategyAction()
            : strategy.getActions().get(0);

        action.setTemplateId(request.getTemplateId());
        action.setChannel(request.getChannel());
        action.setStrategy(strategy);

        if (strategy.getActions().isEmpty()) {
            strategy.getActions().add(action);
        }

        strategyRepository.save(strategy);
        log.info("Template updated successfully for strategy: {}", strategyId);

        return TemplateInfoResponse.builder()
                .strategyId(strategyId)
                .templateId(request.getTemplateId())
                .channel(request.getChannel())
                .build();
    }

    @Override
    @Cacheable(value = "strategyTemplate", key = "#strategyId")
    @Transactional(readOnly = true)
    public TemplateInfoResponse getTemplate(Long strategyId) {
        log.info("Fetching template for strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        if (strategy.getActions().isEmpty()) {
            return TemplateInfoResponse.builder()
                    .strategyId(strategyId)
                    .build();
        }

        StrategyAction action = strategy.getActions().get(0);
        return TemplateInfoResponse.builder()
                .strategyId(strategyId)
                .templateId(action.getTemplateId())
                .channel(action.getChannel())
                .build();
    }

    @Override
    @CacheEvict(value = {"strategyDetails", "strategyTrigger"}, allEntries = true)
    public TriggerConfigResponse configureTrigger(Long strategyId, TriggerConfigRequest request) {
        return updateTriggerInternal(strategyId, request);
    }

    @Override
    @CacheEvict(value = {"strategyDetails", "strategyTrigger"}, allEntries = true)
    public TriggerConfigResponse updateTrigger(Long strategyId, TriggerConfigRequest request) {
        return updateTriggerInternal(strategyId, request);
    }

    private TriggerConfigResponse updateTriggerInternal(Long strategyId, TriggerConfigRequest request) {
        log.info("Updating trigger config for strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        strategy.setTriggerFrequency(request.getTriggerType());
        strategy.setScheduleExpression(request.getScheduleExpression());
        strategy.setEventType(request.getEvent());

        strategyRepository.save(strategy);
        log.info("Trigger config updated successfully for strategy: {}", strategyId);

        return TriggerConfigResponse.builder()
                .strategyId(strategyId)
                .triggerType(request.getTriggerType())
                .scheduleExpression(request.getScheduleExpression())
                .event(request.getEvent())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationResultDTO simulateStrategy(Long strategyId) {
        log.info("Simulating strategy ID: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        // Simplified simulation - in real implementation, this would query cases based on rules
        int estimatedCases = new Random().nextInt(500) + 100;

        List<Map<String, Object>> sampleCases = new ArrayList<>();
        for (int i = 1; i <= Math.min(5, estimatedCases); i++) {
            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("caseId", 1000 + i);
            caseInfo.put("caseNumber", "CASE-2025-" + String.format("%06d", 1000 + i));
            sampleCases.add(caseInfo);
        }

        return SimulationResultDTO.builder()
                .strategyId(strategyId)
                .estimatedCasesAffected(estimatedCases)
                .sampleCases(sampleCases)
                .build();
    }

    // Conversion methods
    private StrategyDTO convertToStrategyDTO(Strategy strategy) {
        return StrategyDTO.builder()
                .id(strategy.getId())
                .name(strategy.getStrategyName())
                .status(strategy.getStatus().name())
                .lastRun(strategy.getLastRunAt())
                .successCount(strategy.getSuccessCount())
                .failureCount(strategy.getFailureCount())
                .triggerType(strategy.getTriggerFrequency())
                .description(strategy.getDescription())
                .createdAt(strategy.getCreatedAt())
                .build();
    }

    private StrategyDetailDTO convertToStrategyDetailDTO(Strategy strategy) {
        List<StrategyRuleDTO> rules = strategy.getRules().stream()
                .map(this::convertToStrategyRuleDTO)
                .collect(Collectors.toList());

        List<StrategyActionDTO> actions = strategy.getActions().stream()
                .map(this::convertToStrategyActionDTO)
                .collect(Collectors.toList());

        return StrategyDetailDTO.builder()
                .id(strategy.getId())
                .name(strategy.getStrategyName())
                .description(strategy.getDescription())
                .triggerType(strategy.getTriggerFrequency())
                .scheduleExpression(strategy.getScheduleExpression())
                .eventType(strategy.getEventType())
                .rules(rules)
                .actions(actions)
                .status(strategy.getStatus().name())
                .priority(strategy.getPriority())
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .build();
    }

    private StrategyRuleDTO convertToStrategyRuleDTO(StrategyRule rule) {
        return StrategyRuleDTO.builder()
                .id(rule.getId())
                .field(rule.getFieldName())
                .operator(rule.getOperator().name())
                .value(rule.getFieldValue())
                .logicalOperator(rule.getLogicalOperator())
                .build();
    }

    private StrategyActionDTO convertToStrategyActionDTO(StrategyAction action) {
        // Extract noticeType from actionConfig if present
        String noticeType = null;
        if (action.getActionConfig() != null && action.getActionConfig().containsKey("noticeType")) {
            noticeType = (String) action.getActionConfig().get("noticeType");
        }

        return StrategyActionDTO.builder()
                .id(action.getId())
                .type(action.getActionType().name())
                .templateId(action.getTemplateId())
                .priority(action.getPriority())
                .channel(action.getChannel())
                .noticeType(noticeType)
                .build();
    }

    private StrategyRule convertToStrategyRule(StrategyRuleDTO dto) {
        // Build conditions map for JSONB field
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("field", dto.getField());
        conditions.put("operator", dto.getOperator());
        conditions.put("value", dto.getValue());

        return StrategyRule.builder()
                .fieldName(dto.getField())
                .operator(RuleOperator.valueOf(dto.getOperator()))
                .fieldValue(dto.getValue() != null ? dto.getValue().toString() : null)
                .conditions(conditions)
                .logicalOperator(dto.getLogicalOperator() != null ? dto.getLogicalOperator() : "AND")
                .isActive(true)
                .build();
    }

    private StrategyAction convertToStrategyAction(StrategyActionDTO dto) {
        // Build actionConfig map for JSONB field
        Map<String, Object> actionConfig = new HashMap<>();
        actionConfig.put("type", dto.getType());
        if (dto.getTemplateId() != null) {
            actionConfig.put("templateId", dto.getTemplateId());
        }
        if (dto.getChannel() != null) {
            actionConfig.put("channel", dto.getChannel());
        }
        if (dto.getNoticeType() != null) {
            actionConfig.put("noticeType", dto.getNoticeType());
        }

        return StrategyAction.builder()
                .actionType(ActionType.valueOf(dto.getType()))
                .actionConfig(actionConfig)
                .templateId(dto.getTemplateId())
                .channel(dto.getChannel())
                .priority(dto.getPriority())
                .isActive(true)
                .build();
    }
}

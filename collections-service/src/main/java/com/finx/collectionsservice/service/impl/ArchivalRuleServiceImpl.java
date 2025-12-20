package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.ArchivalRuleDTO;
import com.finx.collectionsservice.domain.dto.CaseClosureResponse;
import com.finx.collectionsservice.domain.dto.CreateArchivalRuleRequest;
import com.finx.collectionsservice.domain.entity.ArchivalRule;
import com.finx.collectionsservice.domain.enums.RuleStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.ArchivalRuleRepository;
import com.finx.collectionsservice.service.ArchivalRuleService;
import com.finx.collectionsservice.service.CycleClosureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ArchivalRuleServiceImpl implements ArchivalRuleService {

    private final ArchivalRuleRepository archivalRuleRepository;
    private final CycleClosureService cycleClosureService;
    private final CollectionsMapper mapper;

    @Override
    @CacheEvict(value = "archivalRules", allEntries = true)
    public ArchivalRuleDTO createRule(CreateArchivalRuleRequest request, Long userId) {
        if (archivalRuleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new BusinessException("Rule with code " + request.getRuleCode() + " already exists");
        }

        ArchivalRule rule = ArchivalRule.builder()
                .ruleCode(request.getRuleCode())
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .criteria(request.getCriteria())
                .cronExpression(request.getCronExpression())
                .scheduleDescription(request.getScheduleDescription())
                .status(RuleStatus.ACTIVE)
                .isActive(true)
                .createdBy(userId)
                .build();

        rule = archivalRuleRepository.save(rule);
        log.info("Created archival rule: {}", rule.getRuleCode());
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @Cacheable(value = "archivalRules", key = "#id")
    @Transactional(readOnly = true)
    public ArchivalRuleDTO getRuleById(Long id) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivalRuleDTO getRuleByCode(String ruleCode) {
        ArchivalRule rule = archivalRuleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with code: " + ruleCode));
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @Cacheable(value = "archivalRules", key = "'active'")
    @Transactional(readOnly = true)
    public List<ArchivalRuleDTO> getActiveRules() {
        return archivalRuleRepository.findActiveRules().stream()
                .map(mapper::toArchivalRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArchivalRuleDTO> getAllRules(Pageable pageable) {
        return archivalRuleRepository.findAll(pageable)
                .map(mapper::toArchivalRuleDTO);
    }

    @Override
    @CacheEvict(value = "archivalRules", allEntries = true)
    public ArchivalRuleDTO updateRule(Long id, CreateArchivalRuleRequest request, Long userId) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));

        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setCriteria(request.getCriteria());
        rule.setCronExpression(request.getCronExpression());
        rule.setScheduleDescription(request.getScheduleDescription());
        rule.setUpdatedBy(userId);

        rule = archivalRuleRepository.save(rule);
        log.info("Updated archival rule: {}", rule.getRuleCode());
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @CacheEvict(value = "archivalRules", allEntries = true)
    public ArchivalRuleDTO activateRule(Long id, Long userId) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));

        rule.setIsActive(true);
        rule.setStatus(RuleStatus.ACTIVE);
        rule.setUpdatedBy(userId);
        rule = archivalRuleRepository.save(rule);
        log.info("Activated archival rule: {}", rule.getRuleCode());
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @CacheEvict(value = "archivalRules", allEntries = true)
    public ArchivalRuleDTO deactivateRule(Long id, Long userId) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));

        rule.setIsActive(false);
        rule.setStatus(RuleStatus.INACTIVE);
        rule.setUpdatedBy(userId);
        rule = archivalRuleRepository.save(rule);
        log.info("Deactivated archival rule: {}", rule.getRuleCode());
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @CacheEvict(value = "archivalRules", allEntries = true)
    public void deleteRule(Long id, Long userId) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));

        archivalRuleRepository.delete(rule);
        log.info("Deleted archival rule: {} by user: {}", rule.getRuleCode(), userId);
    }

    @Override
    @CacheEvict(value = "archivalRules", allEntries = true)
    public ArchivalRuleDTO executeRule(Long id, Long executedBy) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));

        List<Long> matchingCaseIds = findMatchingCases(rule);

        int casesArchived = 0;
        String result = "SUCCESS";

        if (!matchingCaseIds.isEmpty()) {
            // Execute closure for matching cases
            CaseClosureResponse response = cycleClosureService.closeCasesBulk(matchingCaseIds, "AUTO_ARCHIVAL:" + rule.getRuleCode());
            casesArchived = response.getTotalClosed();
            result = response.getTotalFailed() > 0 ? "PARTIAL" : "SUCCESS";
        }

        // Update rule execution stats
        rule.setExecutionCount(rule.getExecutionCount() + 1);
        rule.setLastExecutionAt(LocalDateTime.now());
        rule.setLastCasesArchived(casesArchived);
        rule.setLastExecutionResult(result);
        rule.setTotalCasesArchived(rule.getTotalCasesArchived() + casesArchived);
        rule = archivalRuleRepository.save(rule);

        log.info("Executed archival rule {}: {} cases archived", rule.getRuleCode(), casesArchived);
        return mapper.toArchivalRuleDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> previewRule(Long id) {
        ArchivalRule rule = archivalRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archival rule not found with id: " + id));

        return findMatchingCases(rule);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processScheduledRules() {
        log.info("Processing scheduled archival rules");
        List<ArchivalRule> dueRules = archivalRuleRepository.findRulesDueForExecution(LocalDateTime.now());

        for (ArchivalRule rule : dueRules) {
            try {
                executeRule(rule.getId(), null);
            } catch (Exception e) {
                log.error("Error executing rule {}: {}", rule.getRuleCode(), e.getMessage());
                rule.setLastExecutionResult("FAILED");
                archivalRuleRepository.save(rule);
            }
        }
    }

    private List<Long> findMatchingCases(ArchivalRule rule) {
        // TODO: Implement criteria-based case matching
        // This would query cases based on the JSON criteria (DPD range, bucket, status, etc.)
        // For now, return empty list
        log.info("Finding matching cases for rule: {} with criteria: {}", rule.getRuleCode(), rule.getCriteria());
        return new ArrayList<>();
    }
}

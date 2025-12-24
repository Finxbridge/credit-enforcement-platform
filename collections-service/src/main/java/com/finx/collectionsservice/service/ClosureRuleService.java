package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Closure Rule management
 */
public interface ClosureRuleService {

    /**
     * Create a new closure rule
     */
    ClosureRuleDTO createRule(CreateClosureRuleRequest request, Long userId);

    /**
     * Update an existing rule
     */
    ClosureRuleDTO updateRule(Long ruleId, CreateClosureRuleRequest request, Long userId);

    /**
     * Get rule by ID
     */
    ClosureRuleDTO getRuleById(Long ruleId);

    /**
     * Get rule by code
     */
    ClosureRuleDTO getRuleByCode(String ruleCode);

    /**
     * Get all active rules
     */
    List<ClosureRuleDTO> getActiveRules();

    /**
     * Search rules with filters
     */
    Page<ClosureRuleDTO> searchRules(String searchTerm, ClosureRuleType ruleType, Boolean isActive, Pageable pageable);

    /**
     * Activate a rule
     */
    ClosureRuleDTO activateRule(Long ruleId, Long userId);

    /**
     * Deactivate a rule
     */
    ClosureRuleDTO deactivateRule(Long ruleId, Long userId);

    /**
     * Delete a rule
     */
    void deleteRule(Long ruleId);

    /**
     * Simulate rule execution - returns eligible cases without actually closing them
     */
    SimulationResultDTO simulateRule(Long ruleId);

    /**
     * Execute rule - actually closes eligible cases
     */
    RuleExecutionDTO executeRule(Long ruleId, Long userId);

    /**
     * Get execution history for a rule
     */
    Page<RuleExecutionDTO> getRuleExecutionHistory(Long ruleId, Pageable pageable);

    /**
     * Get execution by ID
     */
    RuleExecutionDTO getExecutionById(String executionId);

    /**
     * Get recent executions across all rules
     */
    List<RuleExecutionDTO> getRecentExecutions(int limit);

    /**
     * Get cycle closure dashboard stats
     */
    CycleClosureDashboardDTO getDashboardStats();

    /**
     * Validate cron expression
     */
    boolean validateCronExpression(String cronExpression);

    /**
     * Get next scheduled run time for a cron expression
     */
    String getNextScheduledRun(String cronExpression);
}

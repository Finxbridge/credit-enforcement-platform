package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.entity.Case;
import com.finx.strategyengineservice.domain.entity.StrategyRule;

import java.util.List;

/**
 * Service for filtering cases based on strategy rules
 * Builds dynamic queries using JPA Criteria API
 */
public interface CaseFilterService {

    /**
     * Filter allocated cases based on strategy rules
     *
     * @param rules List of strategy rules to apply
     * @return List of cases matching all rules (AND logic)
     */
    List<Case> filterCasesByRules(List<StrategyRule> rules);

    /**
     * Count cases that match strategy rules (for simulation)
     *
     * @param rules List of strategy rules to apply
     * @return Number of cases that would be affected
     */
    Long countCasesByRules(List<StrategyRule> rules);

    /**
     * Filter cases by single rule (used for testing)
     *
     * @param rule Strategy rule to apply
     * @return List of cases matching the rule
     */
    List<Case> filterCasesByRule(StrategyRule rule);
}

package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.dto.CreateRoutingRuleRequest;
import com.finx.noticemanagementservice.domain.dto.RoutingRuleDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateRoutingRuleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RoutingRuleService {

    RoutingRuleDTO createRoutingRule(CreateRoutingRuleRequest request);

    RoutingRuleDTO getRoutingRuleById(Long id);

    RoutingRuleDTO getRoutingRuleByCode(String ruleCode);

    List<RoutingRuleDTO> getActiveRoutingRules();

    List<RoutingRuleDTO> getValidRoutingRules();

    Page<RoutingRuleDTO> getAllRoutingRules(Boolean isActive, String search, Pageable pageable);

    RoutingRuleDTO updateRoutingRule(Long id, UpdateRoutingRuleRequest request);

    RoutingRuleDTO activateRoutingRule(Long id);

    RoutingRuleDTO deactivateRoutingRule(Long id);

    void deleteRoutingRule(Long id);

    /**
     * Find the best matching rule for a given set of criteria
     */
    RoutingRuleDTO findMatchingRule(Map<String, Object> criteria);
}

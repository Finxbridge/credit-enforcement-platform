package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.ArchivalRuleDTO;
import com.finx.collectionsservice.domain.dto.CreateArchivalRuleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArchivalRuleService {

    ArchivalRuleDTO createRule(CreateArchivalRuleRequest request, Long userId);

    ArchivalRuleDTO getRuleById(Long id);

    ArchivalRuleDTO getRuleByCode(String ruleCode);

    List<ArchivalRuleDTO> getActiveRules();

    Page<ArchivalRuleDTO> getAllRules(Pageable pageable);

    ArchivalRuleDTO updateRule(Long id, CreateArchivalRuleRequest request, Long userId);

    ArchivalRuleDTO activateRule(Long id, Long userId);

    ArchivalRuleDTO deactivateRule(Long id, Long userId);

    void deleteRule(Long id, Long userId);

    ArchivalRuleDTO executeRule(Long id, Long executedBy);

    List<Long> previewRule(Long id);

    void processScheduledRules();
}

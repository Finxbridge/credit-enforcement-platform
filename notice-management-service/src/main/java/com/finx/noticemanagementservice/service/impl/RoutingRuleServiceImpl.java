package com.finx.noticemanagementservice.service.impl;

import com.finx.noticemanagementservice.domain.dto.CreateRoutingRuleRequest;
import com.finx.noticemanagementservice.domain.dto.RoutingRuleDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateRoutingRuleRequest;
import com.finx.noticemanagementservice.domain.entity.NoticeVendor;
import com.finx.noticemanagementservice.domain.entity.RoutingRule;
import com.finx.noticemanagementservice.exception.ResourceNotFoundException;
import com.finx.noticemanagementservice.repository.NoticeVendorRepository;
import com.finx.noticemanagementservice.repository.RoutingRuleRepository;
import com.finx.noticemanagementservice.service.AuditLogService;
import com.finx.noticemanagementservice.service.RoutingRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingRuleServiceImpl implements RoutingRuleService {

    private final RoutingRuleRepository routingRuleRepository;
    private final NoticeVendorRepository vendorRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public RoutingRuleDTO createRoutingRule(CreateRoutingRuleRequest request) {
        if (routingRuleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new IllegalArgumentException("Rule with code already exists: " + request.getRuleCode());
        }

        NoticeVendor primaryVendor = null;
        NoticeVendor secondaryVendor = null;
        NoticeVendor fallbackVendor = null;

        if (request.getPrimaryVendorId() != null) {
            primaryVendor = vendorRepository.findById(request.getPrimaryVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Primary vendor not found with id: " + request.getPrimaryVendorId()));
        }
        if (request.getSecondaryVendorId() != null) {
            secondaryVendor = vendorRepository.findById(request.getSecondaryVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Secondary vendor not found with id: " + request.getSecondaryVendorId()));
        }
        if (request.getFallbackVendorId() != null) {
            fallbackVendor = vendorRepository.findById(request.getFallbackVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fallback vendor not found with id: " + request.getFallbackVendorId()));
        }

        RoutingRule rule = RoutingRule.builder()
                .ruleCode(request.getRuleCode())
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .rulePriority(request.getRulePriority())
                .criteria(request.getCriteria())
                .primaryVendor(primaryVendor)
                .secondaryVendor(secondaryVendor)
                .fallbackVendor(fallbackVendor)
                .dispatchSlaHours(request.getDispatchSlaHours())
                .deliverySlaDays(request.getDeliverySlaDays())
                .maxCostPerDispatch(request.getMaxCostPerDispatch())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();

        RoutingRule saved = routingRuleRepository.save(rule);

        auditLogService.logCreate("RoutingRule", saved.getId().toString(), saved.getRuleName(),
                Map.of("ruleCode", saved.getRuleCode(), "criteria", saved.getCriteria()));

        log.info("Created routing rule: {} - {}", saved.getRuleCode(), saved.getRuleName());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutingRuleDTO getRoutingRuleById(Long id) {
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));
        return mapToDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutingRuleDTO getRoutingRuleByCode(String ruleCode) {
        RoutingRule rule = routingRuleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with code: " + ruleCode));
        return mapToDTO(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutingRuleDTO> getActiveRoutingRules() {
        return routingRuleRepository.findByIsActiveTrueOrderByRulePriorityDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutingRuleDTO> getValidRoutingRules() {
        return routingRuleRepository.findValidRules(LocalDate.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoutingRuleDTO> getAllRoutingRules(Boolean isActive, String search, Pageable pageable) {
        return routingRuleRepository.findWithFilters(isActive, search, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public RoutingRuleDTO updateRoutingRule(Long id, UpdateRoutingRuleRequest request) {
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));

        Map<String, Object> oldValue = Map.of(
                "ruleName", rule.getRuleName(),
                "criteria", rule.getCriteria(),
                "isActive", rule.getIsActive()
        );

        List<String> changedFields = new ArrayList<>();

        if (request.getRuleName() != null && !request.getRuleName().equals(rule.getRuleName())) {
            rule.setRuleName(request.getRuleName());
            changedFields.add("ruleName");
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
            changedFields.add("description");
        }
        if (request.getRulePriority() != null) {
            rule.setRulePriority(request.getRulePriority());
            changedFields.add("rulePriority");
        }
        if (request.getCriteria() != null) {
            rule.setCriteria(request.getCriteria());
            changedFields.add("criteria");
        }
        if (request.getPrimaryVendorId() != null) {
            NoticeVendor vendor = vendorRepository.findById(request.getPrimaryVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Primary vendor not found"));
            rule.setPrimaryVendor(vendor);
            changedFields.add("primaryVendorId");
        }
        if (request.getSecondaryVendorId() != null) {
            NoticeVendor vendor = vendorRepository.findById(request.getSecondaryVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Secondary vendor not found"));
            rule.setSecondaryVendor(vendor);
            changedFields.add("secondaryVendorId");
        }
        if (request.getFallbackVendorId() != null) {
            NoticeVendor vendor = vendorRepository.findById(request.getFallbackVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fallback vendor not found"));
            rule.setFallbackVendor(vendor);
            changedFields.add("fallbackVendorId");
        }
        if (request.getDispatchSlaHours() != null) {
            rule.setDispatchSlaHours(request.getDispatchSlaHours());
            changedFields.add("dispatchSlaHours");
        }
        if (request.getDeliverySlaDays() != null) {
            rule.setDeliverySlaDays(request.getDeliverySlaDays());
            changedFields.add("deliverySlaDays");
        }
        if (request.getMaxCostPerDispatch() != null) {
            rule.setMaxCostPerDispatch(request.getMaxCostPerDispatch());
            changedFields.add("maxCostPerDispatch");
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
            changedFields.add("isActive");
        }
        if (request.getValidFrom() != null) {
            rule.setValidFrom(request.getValidFrom());
            changedFields.add("validFrom");
        }
        if (request.getValidUntil() != null) {
            rule.setValidUntil(request.getValidUntil());
            changedFields.add("validUntil");
        }

        RoutingRule saved = routingRuleRepository.save(rule);

        if (!changedFields.isEmpty()) {
            auditLogService.logUpdate("RoutingRule", saved.getId().toString(), saved.getRuleName(),
                    oldValue, Map.of("ruleName", saved.getRuleName(), "criteria", saved.getCriteria()), changedFields);
        }

        log.info("Updated routing rule: {} - changed fields: {}", saved.getRuleCode(), changedFields);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public RoutingRuleDTO activateRoutingRule(Long id) {
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));

        rule.setIsActive(true);
        RoutingRule saved = routingRuleRepository.save(rule);

        auditLogService.logStatusChange("RoutingRule", saved.getId().toString(),
                saved.getRuleName(), "INACTIVE", "ACTIVE");

        log.info("Activated routing rule: {}", saved.getRuleCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public RoutingRuleDTO deactivateRoutingRule(Long id) {
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));

        rule.setIsActive(false);
        RoutingRule saved = routingRuleRepository.save(rule);

        auditLogService.logStatusChange("RoutingRule", saved.getId().toString(),
                saved.getRuleName(), "ACTIVE", "INACTIVE");

        log.info("Deactivated routing rule: {}", saved.getRuleCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteRoutingRule(Long id) {
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));

        auditLogService.logDelete("RoutingRule", rule.getId().toString(), rule.getRuleName(),
                Map.of("ruleCode", rule.getRuleCode(), "criteria", rule.getCriteria()));

        routingRuleRepository.delete(rule);
        log.info("Deleted routing rule: {}", rule.getRuleCode());
    }

    @Override
    @Transactional(readOnly = true)
    public RoutingRuleDTO findMatchingRule(Map<String, Object> criteria) {
        List<RoutingRule> validRules = routingRuleRepository.findValidRules(LocalDate.now());

        for (RoutingRule rule : validRules) {
            if (matchesCriteria(rule.getCriteria(), criteria)) {
                log.debug("Found matching rule: {} for criteria: {}", rule.getRuleCode(), criteria);
                return mapToDTO(rule);
            }
        }

        log.debug("No matching rule found for criteria: {}", criteria);
        return null;
    }

    private boolean matchesCriteria(Map<String, Object> ruleCriteria, Map<String, Object> inputCriteria) {
        if (ruleCriteria == null || ruleCriteria.isEmpty()) {
            return true; // Empty criteria matches everything
        }

        for (Map.Entry<String, Object> entry : ruleCriteria.entrySet()) {
            String key = entry.getKey();
            Object ruleValue = entry.getValue();
            Object inputValue = inputCriteria.get(key);

            if (ruleValue == null) {
                continue; // Null rule value means match anything
            }

            if (inputValue == null) {
                return false; // Rule expects a value but input doesn't have it
            }

            // Handle list criteria (e.g., pincodes: ["400001", "400002"])
            if (ruleValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> ruleList = (List<Object>) ruleValue;
                if (!ruleList.contains(inputValue.toString())) {
                    return false;
                }
            } else if (!ruleValue.toString().equals(inputValue.toString())) {
                return false;
            }
        }

        return true;
    }

    private RoutingRuleDTO mapToDTO(RoutingRule rule) {
        return RoutingRuleDTO.builder()
                .id(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .description(rule.getDescription())
                .rulePriority(rule.getRulePriority())
                .criteria(rule.getCriteria())
                .primaryVendorId(rule.getPrimaryVendor() != null ? rule.getPrimaryVendor().getId() : null)
                .primaryVendorName(rule.getPrimaryVendor() != null ? rule.getPrimaryVendor().getVendorName() : null)
                .secondaryVendorId(rule.getSecondaryVendor() != null ? rule.getSecondaryVendor().getId() : null)
                .secondaryVendorName(rule.getSecondaryVendor() != null ? rule.getSecondaryVendor().getVendorName() : null)
                .fallbackVendorId(rule.getFallbackVendor() != null ? rule.getFallbackVendor().getId() : null)
                .fallbackVendorName(rule.getFallbackVendor() != null ? rule.getFallbackVendor().getVendorName() : null)
                .dispatchSlaHours(rule.getDispatchSlaHours())
                .deliverySlaDays(rule.getDeliverySlaDays())
                .maxCostPerDispatch(rule.getMaxCostPerDispatch())
                .isActive(rule.getIsActive())
                .validFrom(rule.getValidFrom())
                .validUntil(rule.getValidUntil())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .createdBy(rule.getCreatedBy())
                .updatedBy(rule.getUpdatedBy())
                .build();
    }
}

package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.entity.Case;
import com.finx.strategyengineservice.domain.entity.StrategyRule;
import com.finx.strategyengineservice.domain.enums.RuleOperator;
import com.finx.strategyengineservice.exception.BusinessException;
import com.finx.strategyengineservice.service.CaseFilterService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CaseFilterService
 * Builds dynamic JPA Criteria queries based on strategy rules
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseFilterServiceImpl implements CaseFilterService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<Case> filterCasesByRules(List<StrategyRule> rules) {
        if (rules == null || rules.isEmpty()) {
            log.warn("No rules provided for filtering, returning empty list");
            return new ArrayList<>();
        }

        log.info("Filtering cases with {} rules", rules.size());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Case> query = cb.createQuery(Case.class);
        Root<Case> root = query.from(Case.class);

        // Eager fetch loan and customer details to avoid N+1 queries
        // and to ensure nested paths like loan.primaryCustomer.languagePreference work
        Fetch<Case, ?> loanFetch = root.fetch("loan", JoinType.LEFT);
        loanFetch.fetch("primaryCustomer", JoinType.LEFT);

        // Build predicates for all rules
        List<Predicate> filterPredicates = new ArrayList<>();
        int validRulesCount = 0;

        // Add predicates for each rule
        for (StrategyRule rule : rules) {
            try {
                log.info("Processing rule: field={}, operator={}, value={}",
                        rule.getFieldName(), rule.getOperator(), rule.getFieldValue());
                Predicate predicate = buildPredicate(cb, root, rule);
                if (predicate != null) {
                    filterPredicates.add(predicate);
                    validRulesCount++;
                    log.info("Successfully created predicate for field: {}", rule.getFieldName());
                } else {
                    log.warn("Rule {} produced null predicate, field: {}, operator: {}",
                            rule.getId(), rule.getFieldName(), rule.getOperator());
                }
            } catch (Exception e) {
                log.error("Failed to build predicate for rule {}, field={}: {}",
                        rule.getId(), rule.getFieldName(), e.getMessage());
                throw new BusinessException("Invalid rule configuration: " + e.getMessage());
            }
        }

        // If no valid filter predicates were created, return empty list
        // This prevents returning all cases when filters are invalid or don't match
        if (filterPredicates.isEmpty()) {
            log.warn("No valid filter predicates created from {} rules, returning empty list", rules.size());
            return new ArrayList<>();
        }

        log.info("Created {} valid predicates from {} rules", validRulesCount, rules.size());

        // Combine filter predicates with AND (intersection of all filters)
        Predicate filterPredicate;
        if (filterPredicates.size() == 1) {
            filterPredicate = filterPredicates.get(0);
        } else {
            // Always use AND to get intersection of all filter conditions
            filterPredicate = cb.and(filterPredicates.toArray(new Predicate[0]));
        }

        // Combine with base conditions:
        // 1. caseStatus = 'ALLOCATED' (workflow status)
        // 2. status = 200 (ACTIVE cases only, not closed)
        Predicate allocatedPredicate = cb.equal(root.get("caseStatus"), "ALLOCATED");
        Predicate activePredicate = cb.equal(root.get("status"), 200);
        Predicate basePredicate = cb.and(allocatedPredicate, activePredicate);
        Predicate finalPredicate = cb.and(basePredicate, filterPredicate);

        query.where(finalPredicate);

        List<Case> results = entityManager.createQuery(query).getResultList();
        log.info("Found {} ACTIVE cases matching {} filter rules (AND logic)", results.size(), validRulesCount);

        return results;
    }

    @Override
    public Long countCasesByRules(List<StrategyRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return 0L;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Case> root = query.from(Case.class);

        query.select(cb.count(root));

        // Build predicates for all rules
        List<Predicate> filterPredicates = new ArrayList<>();

        // Add predicates for each rule
        for (StrategyRule rule : rules) {
            try {
                Predicate predicate = buildPredicate(cb, root, rule);
                if (predicate != null) {
                    filterPredicates.add(predicate);
                }
            } catch (Exception e) {
                log.error("Failed to build predicate for rule {}: {}", rule.getId(), e.getMessage());
                throw new BusinessException("Invalid rule configuration: " + e.getMessage());
            }
        }

        // If no valid filter predicates were created, return 0
        if (filterPredicates.isEmpty()) {
            log.warn("No valid filter predicates created from {} rules, returning 0", rules.size());
            return 0L;
        }

        // Combine filter predicates with AND (intersection of all filters)
        Predicate filterPredicate;
        if (filterPredicates.size() == 1) {
            filterPredicate = filterPredicates.get(0);
        } else {
            // Always use AND to get intersection of all filter conditions
            filterPredicate = cb.and(filterPredicates.toArray(new Predicate[0]));
        }

        // Combine with base conditions:
        // 1. caseStatus = 'ALLOCATED' (workflow status)
        // 2. status = 200 (ACTIVE cases only, not closed)
        Predicate allocatedPredicate = cb.equal(root.get("caseStatus"), "ALLOCATED");
        Predicate activePredicate = cb.equal(root.get("status"), 200);
        Predicate basePredicate = cb.and(allocatedPredicate, activePredicate);
        Predicate finalPredicate = cb.and(basePredicate, filterPredicate);

        query.where(finalPredicate);

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<Case> filterCasesByRule(StrategyRule rule) {
        return filterCasesByRules(List.of(rule));
    }

    /**
     * Build a predicate for a single rule
     * Handles nested fields like "loan.dpd", "loan.bucket",
     * "loan.outstandingAmount"
     */
    private Predicate buildPredicate(CriteriaBuilder cb, Root<Case> root, StrategyRule rule) {
        String fieldName = rule.getFieldName();
        RuleOperator operator = rule.getOperator();
        String fieldValue = rule.getFieldValue();

        if (fieldName == null || operator == null) {
            log.warn("Rule {} has null fieldName or operator, skipping", rule.getId());
            return null;
        }

        // Get the path to the field (handles nested fields like "loan.dpd")
        Path<?> fieldPath = getFieldPath(root, fieldName);

        if (fieldPath == null) {
            log.warn("Invalid field name '{}' for rule {}. Skipping.", fieldName, rule.getId());
            return null;
        }

        // Build predicate based on operator
        return buildPredicateByOperator(cb, fieldPath, operator, fieldValue, fieldName);
    }

    /**
     * Get field path, handling nested fields using joins for proper filtering
     * Examples: "caseStatus" -> root.get("caseStatus")
     * "loan.dpd" -> root.join("loan").get("dpd")
     * "loan.primaryCustomer.languagePreference" -> root.join("loan").join("primaryCustomer").get("languagePreference")
     */
    private Path<?> getFieldPath(Root<Case> root, String fieldName) {
        try {
            if (fieldName.contains(".")) {
                String[] parts = fieldName.split("\\.");

                int startIndex = 0;
                if (parts.length > 1 && "case".equalsIgnoreCase(parts[0])) {
                    startIndex = 1;
                }

                // Use joins for relationship navigation, get() only for the final attribute
                From<?, ?> currentFrom = root;
                for (int i = startIndex; i < parts.length - 1; i++) {
                    // Use left join for relationships to handle null values
                    currentFrom = currentFrom.join(parts[i], JoinType.LEFT);
                    log.debug("Joined path part: {}", parts[i]);
                }

                // Get the final attribute from the last joined entity
                String finalAttribute = parts[parts.length - 1];
                Path<?> result = currentFrom.get(finalAttribute);
                log.debug("Final path for field '{}': joined to '{}', getting '{}'",
                        fieldName, currentFrom.getJavaType().getSimpleName(), finalAttribute);
                return result;
            } else {
                if ("case".equalsIgnoreCase(fieldName)) {
                    return null;
                }
                return root.get(fieldName);
            }
        } catch (IllegalArgumentException e) {
            // This exception is thrown by JPA when the attribute is not found
            log.warn("Invalid field name '{}': {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * Build predicate based on operator type
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Predicate buildPredicateByOperator(CriteriaBuilder cb, Path<?> fieldPath,
            RuleOperator operator, String fieldValue,
            String fieldName) {
        switch (operator) {
            case EQUALS:
                // For String fields, use case-insensitive matching
                if (fieldPath.getJavaType().equals(String.class)) {
                    return cb.equal(cb.lower((Expression<String>) fieldPath), fieldValue.toLowerCase());
                }
                return cb.equal(fieldPath, convertValue(fieldPath, fieldValue));

            case NOT_EQUALS:
                // For String fields, use case-insensitive matching
                if (fieldPath.getJavaType().equals(String.class)) {
                    return cb.notEqual(cb.lower((Expression<String>) fieldPath), fieldValue.toLowerCase());
                }
                return cb.notEqual(fieldPath, convertValue(fieldPath, fieldValue));

            case GREATER_THAN:
                return cb.greaterThan((Expression<Comparable>) fieldPath,
                        (Comparable) convertValue(fieldPath, fieldValue));

            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo((Expression<Comparable>) fieldPath,
                        (Comparable) convertValue(fieldPath, fieldValue));

            case LESS_THAN:
                return cb.lessThan((Expression<Comparable>) fieldPath,
                        (Comparable) convertValue(fieldPath, fieldValue));

            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo((Expression<Comparable>) fieldPath,
                        (Comparable) convertValue(fieldPath, fieldValue));

            case IN:
                String[] values = fieldValue.split(",");
                // For String fields, use case-insensitive matching
                if (fieldPath.getJavaType().equals(String.class)) {
                    List<Predicate> inPredicates = new ArrayList<>();
                    Expression<String> lowerFieldPath = cb.lower((Expression<String>) fieldPath);
                    for (String val : values) {
                        inPredicates.add(cb.equal(lowerFieldPath, val.trim().toLowerCase()));
                    }
                    return cb.or(inPredicates.toArray(new Predicate[0]));
                } else {
                    List<Object> convertedValues = new ArrayList<>();
                    for (String val : values) {
                        convertedValues.add(convertValue(fieldPath, val.trim()));
                    }
                    return fieldPath.in(convertedValues);
                }

            case NOT_IN:
                String[] notInValues = fieldValue.split(",");
                // For String fields, use case-insensitive matching
                if (fieldPath.getJavaType().equals(String.class)) {
                    List<Predicate> notInPredicates = new ArrayList<>();
                    Expression<String> lowerFieldPathNotIn = cb.lower((Expression<String>) fieldPath);
                    for (String val : notInValues) {
                        notInPredicates.add(cb.equal(lowerFieldPathNotIn, val.trim().toLowerCase()));
                    }
                    return cb.not(cb.or(notInPredicates.toArray(new Predicate[0])));
                } else {
                    List<Object> convertedNotInValues = new ArrayList<>();
                    for (String val : notInValues) {
                        convertedNotInValues.add(convertValue(fieldPath, val.trim()));
                    }
                    return cb.not(fieldPath.in(convertedNotInValues));
                }

            case CONTAINS:
                return cb.like(cb.lower((Expression<String>) fieldPath),
                        "%" + fieldValue.toLowerCase() + "%");

            case NOT_CONTAINS:
                return cb.notLike(cb.lower((Expression<String>) fieldPath),
                        "%" + fieldValue.toLowerCase() + "%");

            case STARTS_WITH:
                return cb.like(cb.lower((Expression<String>) fieldPath),
                        fieldValue.toLowerCase() + "%");

            case ENDS_WITH:
                return cb.like(cb.lower((Expression<String>) fieldPath),
                        "%" + fieldValue.toLowerCase());

            case BETWEEN:
                String[] betweenValues = fieldValue.split(",");
                if (betweenValues.length != 2) {
                    throw new BusinessException("BETWEEN operator requires two values separated by comma");
                }
                return cb.between((Expression<Comparable>) fieldPath,
                        (Comparable) convertValue(fieldPath, betweenValues[0].trim()),
                        (Comparable) convertValue(fieldPath, betweenValues[1].trim()));

            case IS_NULL:
                return cb.isNull(fieldPath);

            case IS_NOT_NULL:
                return cb.isNotNull(fieldPath);

            default:
                throw new BusinessException("Unsupported operator: " + operator);
        }
    }

    /**
     * Convert string value to appropriate type based on field type
     */
    private Object convertValue(Path<?> fieldPath, String value) {
        if (value == null) {
            return null;
        }

        Class<?> fieldType = fieldPath.getJavaType();

        try {
            if (fieldType.equals(String.class)) {
                return value;
            } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                return new BigDecimal(value).intValueExact();
            } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                return new BigDecimal(value).longValueExact();
            } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                return Double.parseDouble(value);
            } else if (fieldType.equals(BigDecimal.class)) {
                return new BigDecimal(value);
            } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                return Boolean.parseBoolean(value);
            } else if (fieldType.equals(LocalDate.class)) {
                return LocalDate.parse(value, DATE_FORMATTER);
            } else if (fieldType.equals(LocalDateTime.class)) {
                return LocalDateTime.parse(value);
            } else {
                // Default: return as string
                return value;
            }
        } catch (Exception e) {
            throw new BusinessException("Invalid value '" + value + "' for field type " + fieldType.getSimpleName());
        }
    }
}

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

        // Eager fetch loan details to avoid N+1 queries
        root.fetch("loan", JoinType.LEFT);

        // Build predicates for all rules
        List<Predicate> predicates = new ArrayList<>();

        // Always filter only ALLOCATED cases
        predicates.add(cb.equal(root.get("caseStatus"), "ALLOCATED"));

        // Add predicates for each rule
        for (StrategyRule rule : rules) {
            try {
                Predicate predicate = buildPredicate(cb, root, rule);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            } catch (Exception e) {
                log.error("Failed to build predicate for rule {}: {}", rule.getId(), e.getMessage());
                throw new BusinessException("Invalid rule configuration: " + e.getMessage());
            }
        }

        // Combine predicates with AND or OR based on logical operator
        Predicate finalPredicate;
        if (predicates.size() == 1) {
            finalPredicate = predicates.get(0);
        } else {
            // Check if rule has logical operator (default is AND)
            String logicalOp = rules.get(0).getLogicalOperator() != null
                    ? rules.get(0).getLogicalOperator().toUpperCase()
                    : "AND";

            if ("OR".equals(logicalOp)) {
                finalPredicate = cb.or(predicates.toArray(new Predicate[0]));
            } else {
                finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
            }
        }

        query.where(finalPredicate);

        List<Case> results = entityManager.createQuery(query).getResultList();
        log.info("Found {} cases matching {} rules", results.size(), rules.size());

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
        List<Predicate> predicates = new ArrayList<>();

        // Always filter only ALLOCATED cases
        predicates.add(cb.equal(root.get("caseStatus"), "ALLOCATED"));

        // Add predicates for each rule
        for (StrategyRule rule : rules) {
            try {
                Predicate predicate = buildPredicate(cb, root, rule);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            } catch (Exception e) {
                log.error("Failed to build predicate for rule {}: {}", rule.getId(), e.getMessage());
                throw new BusinessException("Invalid rule configuration: " + e.getMessage());
            }
        }

        // Combine predicates
        Predicate finalPredicate;
        if (predicates.size() == 1) {
            finalPredicate = predicates.get(0);
        } else {
            String logicalOp = rules.get(0).getLogicalOperator() != null
                    ? rules.get(0).getLogicalOperator().toUpperCase()
                    : "AND";

            if ("OR".equals(logicalOp)) {
                finalPredicate = cb.or(predicates.toArray(new Predicate[0]));
            } else {
                finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
            }
        }

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

        // Build predicate based on operator
        return buildPredicateByOperator(cb, fieldPath, operator, fieldValue, fieldName);
    }

    /**
     * Get field path, handling nested fields
     * Examples: "caseStatus" -> root.get("caseStatus")
     * "loan.dpd" -> root.get("loan").get("dpd")
     * "loan.bucket" -> root.get("loan").get("bucket")
     */
    private Path<?> getFieldPath(Root<Case> root, String fieldName) {
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.");
            Path<?> path = root;

            for (String part : parts) {
                path = path.get(part);
            }

            return path;
        } else {
            return root.get(fieldName);
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
                return cb.equal(fieldPath, convertValue(fieldPath, fieldValue));

            case NOT_EQUALS:
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
                List<Object> convertedValues = new ArrayList<>();
                for (String val : values) {
                    convertedValues.add(convertValue(fieldPath, val.trim()));
                }
                return fieldPath.in(convertedValues);

            case NOT_IN:
                String[] notInValues = fieldValue.split(",");
                List<Object> convertedNotInValues = new ArrayList<>();
                for (String val : notInValues) {
                    convertedNotInValues.add(convertValue(fieldPath, val.trim()));
                }
                return cb.not(fieldPath.in(convertedNotInValues));

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
                return Integer.parseInt(value);
            } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                return Long.parseLong(value);
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

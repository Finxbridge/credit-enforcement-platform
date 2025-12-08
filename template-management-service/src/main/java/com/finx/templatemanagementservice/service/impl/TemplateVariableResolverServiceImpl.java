package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.domain.entity.Case;
import com.finx.templatemanagementservice.domain.entity.Template;
import com.finx.templatemanagementservice.domain.entity.TemplateVariable;
import com.finx.templatemanagementservice.domain.entity.VariableDefinition;
import com.finx.templatemanagementservice.exception.ResourceNotFoundException;
import com.finx.templatemanagementservice.repository.CaseRepository;
import com.finx.templatemanagementservice.repository.TemplateRepository;
import com.finx.templatemanagementservice.repository.VariableDefinitionRepository;
import com.finx.templatemanagementservice.service.TemplateVariableResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Service implementation for resolving template variables from case data
 * Uses reflection-based extraction with centralized variable registry
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TemplateVariableResolverServiceImpl implements TemplateVariableResolverService {

    private final TemplateRepository templateRepository;
    private final CaseRepository caseRepository;
    private final VariableDefinitionRepository variableDefinitionRepository;

    // Date formatters
    private static final DateTimeFormatter DATE_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_MMDDYYYY = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Currency formatter
    private static final NumberFormat CURRENCY_INR = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    @Cacheable(value = "templateVariables", key = "#templateId + '_' + #caseId")
    public Map<String, Object> resolveVariablesForTemplate(Long templateId, Long caseId, Map<String, Object> additionalContext) {
        log.debug("Resolving variables for template: {} and case: {}", templateId, caseId);

        // Fetch template with variables
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + templateId));

        // Fetch case with loan and customer details
        Case caseEntity = caseRepository.findByIdWithLoanAndCustomers(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));

        Map<String, Object> resolvedVariables = new HashMap<>();

        // Resolve each template variable
        for (TemplateVariable templateVar : template.getVariables()) {
            String variableKey = templateVar.getVariableKey();
            Object value = resolveVariableValueInternal(variableKey, caseEntity);

            // Use default value if resolution failed
            if (value == null && templateVar.getDefaultValue() != null) {
                value = templateVar.getDefaultValue();
            }

            resolvedVariables.put(templateVar.getVariableName(), value);
        }

        // Add additional context variables if provided
        if (additionalContext != null && !additionalContext.isEmpty()) {
            resolvedVariables.putAll(additionalContext);
        }

        log.debug("Resolved {} variables for template: {}", resolvedVariables.size(), templateId);
        return resolvedVariables;
    }

    @Override
    public Object resolveVariableValue(String variableKey, Long caseId) {
        log.debug("Resolving variable: {} for case: {}", variableKey, caseId);

        // Fetch case with loan and customer details
        Case caseEntity = caseRepository.findByIdWithLoanAndCustomers(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));

        return resolveVariableValueInternal(variableKey, caseEntity);
    }

    /**
     * Internal method to resolve variable value from case entity
     */
    private Object resolveVariableValueInternal(String variableKey, Case caseEntity) {
        try {
            // Fetch variable definition
            VariableDefinition varDef = variableDefinitionRepository.findByVariableKey(variableKey)
                    .orElse(null);

            if (varDef == null) {
                log.warn("Variable definition not found for key: {}", variableKey);
                return null;
            }

            // Extract raw value using entity path
            Object rawValue = extractValueFromPath(caseEntity, varDef.getEntityPath());

            if (rawValue == null) {
                return varDef.getDefaultValue();
            }

            // Apply transformer if specified
            if (varDef.getTransformer() != null && !varDef.getTransformer().isEmpty()) {
                return applyTransformer(rawValue, varDef.getTransformer());
            }

            return rawValue;

        } catch (Exception e) {
            log.error("Error resolving variable: {}", variableKey, e);
            return null;
        }
    }

    @Override
    public Object extractValueFromPath(Object rootObject, String entityPath) {
        if (rootObject == null || entityPath == null || entityPath.isEmpty()) {
            return null;
        }

        try {
            String[] parts = entityPath.split("\\.");
            Object current = rootObject;

            for (String part : parts) {
                if (current == null) {
                    return null;
                }

                // Build getter method name (e.g., "loan" -> "getLoan")
                String methodName = "get" + part.substring(0, 1).toUpperCase() + part.substring(1);
                current = current.getClass().getMethod(methodName).invoke(current);
            }

            return current;

        } catch (Exception e) {
            log.error("Error extracting value from path: {}", entityPath, e);
            return null;
        }
    }

    /**
     * Apply transformer to raw value
     */
    private Object applyTransformer(Object rawValue, String transformer) {
        if (rawValue == null) {
            return null;
        }

        try {
            switch (transformer.toUpperCase()) {
                // Date transformers
                case "DATE_DDMMYYYY":
                    return formatDate(rawValue, DATE_DDMMYYYY);

                case "DATE_MMDDYYYY":
                    return formatDate(rawValue, DATE_MMDDYYYY);

                case "DATE_YYYYMMDD":
                    return formatDate(rawValue, DATE_YYYYMMDD);

                case "DATETIME_FULL":
                    return formatDateTime(rawValue, DATETIME_FULL);

                // Currency transformers
                case "CURRENCY_INR":
                    return formatCurrency(rawValue, CURRENCY_INR);

                case "CURRENCY_PLAIN":
                    return formatNumber(rawValue, 2);

                // String transformers
                case "UPPERCASE":
                    return rawValue.toString().toUpperCase();

                case "LOWERCASE":
                    return rawValue.toString().toLowerCase();

                case "CAPITALIZE":
                    return capitalize(rawValue.toString());

                case "TRIM":
                    return rawValue.toString().trim();

                // Number transformers
                case "NUMBER_FORMAT":
                    return formatNumber(rawValue, 0);

                case "NUMBER_2_DECIMALS":
                    return formatNumber(rawValue, 2);

                case "PERCENTAGE":
                    return formatPercentage(rawValue);

                // Boolean transformers
                case "YES_NO":
                    return formatBoolean(rawValue, "Yes", "No");

                case "TRUE_FALSE":
                    return formatBoolean(rawValue, "True", "False");

                // Default: return as-is
                default:
                    log.warn("Unknown transformer: {}", transformer);
                    return rawValue;
            }
        } catch (Exception e) {
            log.error("Error applying transformer: {} to value: {}", transformer, rawValue, e);
            return rawValue;
        }
    }

    /**
     * Format date value
     */
    private String formatDate(Object value, DateTimeFormatter formatter) {
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(formatter);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate().format(formatter);
        }
        return value.toString();
    }

    /**
     * Format datetime value
     */
    private String formatDateTime(Object value, DateTimeFormatter formatter) {
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(formatter);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay().format(formatter);
        }
        return value.toString();
    }

    /**
     * Format currency value
     */
    private String formatCurrency(Object value, NumberFormat formatter) {
        if (value instanceof BigDecimal) {
            return formatter.format(((BigDecimal) value).doubleValue());
        } else if (value instanceof Number) {
            return formatter.format(((Number) value).doubleValue());
        }
        return value.toString();
    }

    /**
     * Format number with specified decimal places
     */
    private String formatNumber(Object value, int decimals) {
        if (value instanceof BigDecimal) {
            return String.format("%." + decimals + "f", ((BigDecimal) value).doubleValue());
        } else if (value instanceof Number) {
            return String.format("%." + decimals + "f", ((Number) value).doubleValue());
        }
        return value.toString();
    }

    /**
     * Format percentage
     */
    private String formatPercentage(Object value) {
        if (value instanceof Number) {
            return String.format("%.2f%%", ((Number) value).doubleValue());
        }
        return value.toString();
    }

    /**
     * Format boolean value
     */
    private String formatBoolean(Object value, String trueValue, String falseValue) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? trueValue : falseValue;
        }
        return value.toString();
    }

    /**
     * Capitalize first letter of each word
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }

        return result.toString().trim();
    }
}

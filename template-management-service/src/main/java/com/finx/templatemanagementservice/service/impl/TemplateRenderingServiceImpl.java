package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.domain.entity.Template;
import com.finx.templatemanagementservice.domain.entity.TemplateContent;
import com.finx.templatemanagementservice.exception.BusinessException;
import com.finx.templatemanagementservice.exception.ResourceNotFoundException;
import com.finx.templatemanagementservice.repository.TemplateRepository;
import com.finx.templatemanagementservice.service.TemplateRenderingService;
import com.finx.templatemanagementservice.service.TemplateVariableResolverService;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Service implementation for rendering templates using Handlebars
 * Provides template rendering with custom helpers for formatting
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class TemplateRenderingServiceImpl implements TemplateRenderingService {

    private final TemplateRepository templateRepository;
    private final TemplateVariableResolverService variableResolverService;
    private final Handlebars handlebars;

    /**
     * Constructor initializes Handlebars with custom helpers
     */
    public TemplateRenderingServiceImpl(TemplateRepository templateRepository,
                                       TemplateVariableResolverService variableResolverService) {
        this.templateRepository = templateRepository;
        this.variableResolverService = variableResolverService;
        this.handlebars = new Handlebars();
        registerCustomHelpers();
    }

    /**
     * Register custom Handlebars helpers for formatting
     */
    private void registerCustomHelpers() {
        // Date formatting helper
        handlebars.registerHelper("formatDate", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }

                String format = options.param(0, "dd/MM/yyyy");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

                if (context instanceof LocalDate) {
                    return ((LocalDate) context).format(formatter);
                } else if (context instanceof LocalDateTime) {
                    return ((LocalDateTime) context).toLocalDate().format(formatter);
                }

                return context.toString();
            }
        });

        // Currency formatting helper
        handlebars.registerHelper("formatCurrency", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }

                String locale = options.param(0, "en_IN");
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(
                    locale.equals("en_IN") ? new Locale("en", "IN") : Locale.US
                );

                if (context instanceof BigDecimal) {
                    return currencyFormat.format(((BigDecimal) context).doubleValue());
                } else if (context instanceof Number) {
                    return currencyFormat.format(((Number) context).doubleValue());
                }

                return context.toString();
            }
        });

        // Number formatting helper
        handlebars.registerHelper("formatNumber", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }

                int decimals = options.param(0, 2);

                if (context instanceof BigDecimal) {
                    return String.format("%." + decimals + "f", ((BigDecimal) context).doubleValue());
                } else if (context instanceof Number) {
                    return String.format("%." + decimals + "f", ((Number) context).doubleValue());
                }

                return context.toString();
            }
        });

        // Uppercase helper
        handlebars.registerHelper("upper", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                return context != null ? context.toString().toUpperCase() : "";
            }
        });

        // Lowercase helper
        handlebars.registerHelper("lower", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                return context != null ? context.toString().toLowerCase() : "";
            }
        });

        // Capitalize helper
        handlebars.registerHelper("capitalize", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }

                String str = context.toString();
                if (str.isEmpty()) {
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
        });

        // Default value helper
        handlebars.registerHelper("default", new Helper<Object>() {
            @Override
            public Object apply(Object context, Options options) throws IOException {
                if (context == null || context.toString().isEmpty()) {
                    return options.param(0, "");
                }
                return context;
            }
        });
    }

    @Override
    @Cacheable(value = "renderedTemplates", key = "#templateContent.hashCode() + '_' + #variables.hashCode()")
    public String renderTemplate(String templateContent, Map<String, Object> variables) {
        if (templateContent == null || templateContent.isEmpty()) {
            log.warn("Empty template content provided for rendering");
            return "";
        }

        try {
            log.debug("Rendering template with {} variables", variables != null ? variables.size() : 0);

            // Compile Handlebars template
            com.github.jknack.handlebars.Template hbsTemplate = handlebars.compileInline(templateContent);

            // Render with variables
            String rendered = hbsTemplate.apply(variables != null ? variables : Map.of());

            log.debug("Template rendered successfully");
            return rendered;

        } catch (IOException e) {
            log.error("Error rendering template", e);
            throw new BusinessException("Failed to render template: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "renderedTemplatesForCase", key = "#templateId + '_' + #caseId")
    public String renderTemplateForCase(Long templateId, Long caseId, Map<String, Object> additionalContext) {
        log.debug("Rendering template: {} for case: {}", templateId, caseId);

        // Fetch template
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + templateId));

        // Get template content (default to English)
        TemplateContent content = template.getContents().stream()
                .filter(c -> "en".equals(c.getLanguageCode()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Template content not found for template: " + templateId));

        // Resolve variables from case data
        Map<String, Object> resolvedVariables = variableResolverService.resolveVariablesForTemplate(
                templateId, caseId, additionalContext
        );

        // Render template content
        return renderTemplate(content.getContent(), resolvedVariables);
    }

    @Override
    public String renderSubject(String subject, Map<String, Object> variables) {
        if (subject == null || subject.isEmpty()) {
            return "";
        }

        try {
            com.github.jknack.handlebars.Template hbsTemplate = handlebars.compileInline(subject);
            return hbsTemplate.apply(variables != null ? variables : Map.of());

        } catch (IOException e) {
            log.error("Error rendering subject", e);
            return subject; // Return original subject if rendering fails
        }
    }
}

package com.finx.templatemanagementservice.exception;

/**
 * Exception thrown when template is not found
 */
public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(Long id) {
        super("Template not found with id: " + id);
    }

    public TemplateNotFoundException(String field, String value) {
        super(String.format("Template not found with %s: %s", field, value));
    }
}

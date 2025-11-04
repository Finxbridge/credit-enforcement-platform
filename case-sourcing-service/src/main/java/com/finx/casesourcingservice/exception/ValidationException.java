package com.finx.casesourcingservice.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Getter
public class ValidationException extends BaseException {

    private List<Map<String, Object>> errors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, 400);
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR",
                String.format("Validation failed for field '%s': %s", field, message),
                400);
    }

    public ValidationException(String message, List<Map<String, Object>> errors) {
        super("VALIDATION_ERROR", message, 400);
        this.errors = errors;
    }
}

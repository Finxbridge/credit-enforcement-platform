package com.finx.common.exception;

/**
 * Exception thrown when validation fails
 *
 * @author CMS-NMS Team
 * @version 1.0.0
 */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, 400);
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR",
              String.format("Validation failed for field '%s': %s", field, message),
              400);
    }
}

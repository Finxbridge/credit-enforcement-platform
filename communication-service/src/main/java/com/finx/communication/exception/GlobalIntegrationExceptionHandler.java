package com.finx.communication.exception;

import com.finx.communication.domain.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Communication Service
 * Handles all exceptions and returns standardized CommonResponse format
 *
 * All exceptions return:
 * { "status": "failure", "payload": null, "message": "...", "errorCode": "..." }
 *
 * @version 2.0.0 - Updated to use CommonResponse with "payload" field
 */
@Slf4j
@RestControllerAdvice
public class GlobalIntegrationExceptionHandler {

    /**
     * Handle validation errors (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.<Void>builder()
                        .status("failure")
                        .message("Validation failed")
                        .errorCode("VALIDATION_ERROR")
                        .errors(errors)
                        .build());
    }

    /**
     * Handle configuration not found
     */
    @ExceptionHandler(ConfigurationNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleConfigurationNotFound(ConfigurationNotFoundException ex) {
        log.error("Configuration not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.failure(ex.getMessage(), "CONFIG_NOT_FOUND"));
    }

    /**
     * Handle API call exceptions
     */
    @ExceptionHandler(ApiCallException.class)
    public ResponseEntity<CommonResponse<Void>> handleApiCallException(ApiCallException ex) {
        log.error("API call failed: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(CommonResponse.failure("External API call failed: " + ex.getMessage(), "API_CALL_FAILED"));
    }

    /**
     * Handle integration exceptions
     */
    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<CommonResponse<Void>> handleIntegrationException(IntegrationException ex) {
        log.error("Integration error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(ex.getMessage(), "INTEGRATION_ERROR"));
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(
                        ex.getMessage() != null ? ex.getMessage() : "Invalid argument",
                        "INVALID_ARGUMENT"
                ));
    }

    /**
     * Handle null pointer exceptions
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<CommonResponse<Void>> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure("A required value was null", "NULL_POINTER_ERROR"));
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(
                        ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                        "RUNTIME_ERROR"
                ));
    }

    /**
     * Handle all other exceptions (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}

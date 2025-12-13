package com.finx.templatemanagementservice.exception;

import com.finx.templatemanagementservice.domain.dto.CommonResponse;
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
 * Global exception handler for the application
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleTemplateNotFoundException(TemplateNotFoundException ex) {
        log.error("Template not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.failure("Template not found: " + ex.getMessage(), "TEMPLATE_NOT_FOUND"));
    }

    @ExceptionHandler(TemplateAlreadyExistsException.class)
    public ResponseEntity<CommonResponse<Void>> handleTemplateAlreadyExistsException(TemplateAlreadyExistsException ex) {
        log.error("Template already exists: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(CommonResponse.failure("Template already exists: " + ex.getMessage(), "TEMPLATE_ALREADY_EXISTS"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(ex.getMessage(), "BUSINESS_ERROR"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure("Validation failed", "VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }
}

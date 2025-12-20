package com.finx.agencymanagement.exception;

import com.finx.agencymanagement.domain.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for Agency Management Service
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.error("Validation error on {}: {}", request.getDescription(false), ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(
                        "Validation failed",
                        "VALIDATION_ERROR",
                        errors));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CommonResponse<Void>> handleCustomValidationException(
            ValidationException ex,
            WebRequest request) {

        log.error("Validation error on {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(
                        ex.getMessage(),
                        ex.getErrorCode(),
                        ex.getErrors()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.error("Resource not found on {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.failure(
                        ex.getMessage(),
                        ex.getErrorCode()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.error("Illegal argument on {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(
                        ex.getMessage() != null ? ex.getMessage() : "Invalid argument",
                        "INVALID_ARGUMENT"));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<CommonResponse<Void>> handleNullPointerException(
            NullPointerException ex,
            WebRequest request) {

        log.error("Null pointer on {}: {}", request.getDescription(false), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(
                        "A required value was null",
                        "NULL_POINTER_ERROR"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(
            BusinessException ex,
            WebRequest request) {

        log.error("Business exception on {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(
                        ex.getMessage(),
                        ex.getErrorCode() != null ? ex.getErrorCode() : "BUSINESS_ERROR"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<Void>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        log.error("Runtime exception on {}: {}", request.getDescription(false), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(
                        ex.getMessage() != null ? ex.getMessage()
                                : "An unexpected error occurred",
                        "RUNTIME_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Unhandled exception on {}: {}", request.getDescription(false), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(
                        "An internal server error occurred",
                        "INTERNAL_ERROR"));
    }
}

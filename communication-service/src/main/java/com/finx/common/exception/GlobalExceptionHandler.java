package com.finx.common.exception;

import com.finx.common.dto.CommonResponse;
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
 * Global Exception Handler
 * Handles all exceptions across microservices and returns standardized
 * CommonResponse
 *
 * All exceptions return:
 * { "status": "failure", "payload": null, "message": "...", "errorCode": "..."
 * }
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handle API call exceptions (upstream service failures)
         */
        @ExceptionHandler(ApiCallException.class)
        public ResponseEntity<CommonResponse<Void>> handleApiCallException(
                        ApiCallException ex,
                        WebRequest request) {

                log.error("API call exception on {}: {}", request.getDescription(false), ex.getMessage());

                return ResponseEntity.status(HttpStatus.BAD_GATEWAY) // 502 for upstream errors
                                .body(CommonResponse.failure(
                                                ex.getMessage() != null ? ex.getMessage()
                                                                : "Error calling upstream service",
                                                "UPSTREAM_SERVICE_ERROR"));
        }

        /**
         * Handle configuration not found exceptions
         */
        @ExceptionHandler(ConfigurationNotFoundException.class)
        public ResponseEntity<CommonResponse<Void>> handleConfigurationNotFoundException(
                        ConfigurationNotFoundException ex,
                        WebRequest request) {

                log.error("Configuration not found on {}: {}", request.getDescription(false), ex.getMessage());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(CommonResponse.failure(
                                                ex.getMessage() != null ? ex.getMessage()
                                                                : "A required configuration is missing",
                                                "CONFIGURATION_ERROR"));
        }

        /**
         * Handle validation errors (Bean Validation)
         * Returns all field-level validation errors
         */
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

        /**
         * Handle illegal argument exceptions
         */
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

        /**
         * Handle null pointer exceptions
         */
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

        /**
         * Handle resource not found exceptions
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<CommonResponse<Void>> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        WebRequest request) {

                log.error("Resource not found on {}: {}", request.getDescription(false), ex.getMessage());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(CommonResponse.failure(
                                                ex.getMessage(),
                                                "RESOURCE_NOT_FOUND"));
        }

        /**
         * Handle unauthorized access exceptions
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<CommonResponse<Void>> handleUnauthorizedException(
                        UnauthorizedException ex,
                        WebRequest request) {

                log.error("Unauthorized access on {}: {}", request.getDescription(false), ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(CommonResponse.failure(
                                                ex.getMessage(),
                                                "UNAUTHORIZED"));
        }

        /**
         * Handle forbidden access exceptions
         */
        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<CommonResponse<Void>> handleForbiddenException(
                        ForbiddenException ex,
                        WebRequest request) {

                log.error("Forbidden access on {}: {}", request.getDescription(false), ex.getMessage());

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(CommonResponse.failure(
                                                ex.getMessage(),
                                                "FORBIDDEN"));
        }

        /**
         * Handle business logic exceptions
         */
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

        /**
         * Handle runtime exceptions
         */
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

        /**
         * Handle all other exceptions (catch-all)
         */
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

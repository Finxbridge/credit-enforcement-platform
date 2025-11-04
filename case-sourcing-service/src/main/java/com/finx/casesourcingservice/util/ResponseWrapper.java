package com.finx.casesourcingservice.util;

import com.finx.casesourcingservice.domain.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;

/**
 * Response Wrapper Utility
 * Helper class to wrap responses into CommonResponse format
 *
 * Usage Examples:
 * 1. ResponseWrapper.ok(data)
 * 2. ResponseWrapper.ok("Success message", data)
 * 3. ResponseWrapper.error("Error message")
 * 4. ResponseWrapper.error("Error message", "ERR_CODE")
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public class ResponseWrapper {

    private ResponseWrapper() {
        // Utility class - prevent instantiation
    }

    // ============================================
    // SUCCESS RESPONSES (200 OK)
    // ============================================

    /**
     * Create 200 OK response with payload
     * Usage: return ResponseWrapper.ok(responseData);
     */
    public static <T> ResponseEntity<CommonResponse<T>> ok(T payload) {
        return ResponseEntity.ok(CommonResponse.success(payload));
    }

    /**
     * Create 200 OK response with message and payload
     * Usage: return ResponseWrapper.ok("User created successfully", userData);
     */
    public static <T> ResponseEntity<CommonResponse<T>> ok(String message, T payload) {
        return ResponseEntity.ok(CommonResponse.success(message, payload));
    }

    /**
     * Create 200 OK response with only message (no payload)
     * Usage: return ResponseWrapper.okMessage("Operation completed");
     */
    public static <T> ResponseEntity<CommonResponse<T>> okMessage(String message) {
        return ResponseEntity.ok(CommonResponse.successMessage(message));
    }

    // ============================================
    // CREATED RESPONSES (201)
    // ============================================

    /**
     * Create 201 CREATED response with payload
     * Usage: return ResponseWrapper.created(newUser);
     */
    public static <T> ResponseEntity<CommonResponse<T>> created(T payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Resource created successfully", payload));
    }

    /**
     * Create 201 CREATED response with message and payload
     * Usage: return ResponseWrapper.created("User created", newUser);
     */
    public static <T> ResponseEntity<CommonResponse<T>> created(String message, T payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(message, payload));
    }

    // ============================================
    // ACCEPTED RESPONSES (202)
    // ============================================

    /**
     * Create 202 ACCEPTED response with payload
     * Usage: return ResponseWrapper.accepted(batchInfo);
     */
    public static <T> ResponseEntity<CommonResponse<T>> accepted(T payload) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Request accepted for processing", payload));
    }

    /**
     * Create 202 ACCEPTED response with message and payload
     * Usage: return ResponseWrapper.accepted("Upload initiated", batchInfo);
     */
    public static <T> ResponseEntity<CommonResponse<T>> accepted(String message, T payload) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success(message, payload));
    }

    // ============================================
    // ERROR RESPONSES (400 BAD REQUEST)
    // ============================================

    /**
     * Create 400 BAD REQUEST response with message
     * Usage: return ResponseWrapper.badRequest("Invalid input");
     */
    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message));
    }

    /**
     * Create 400 BAD REQUEST response with message and error code
     * Usage: return ResponseWrapper.badRequest("Invalid input", "VAL_001");
     */
    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message, String errorCode) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message, errorCode));
    }

    /**
     * Create 400 BAD REQUEST response with message and validation errors
     * Usage: return ResponseWrapper.badRequest("Validation failed",
     * validationErrors);
     */
    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message, Map<String, Object> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message, errors));
    }

    // ============================================
    // UNAUTHORIZED RESPONSES (401)
    // ============================================

    /**
     * Create 401 UNAUTHORIZED response
     * Usage: return ResponseWrapper.unauthorized("Invalid credentials");
     */
    public static <T> ResponseEntity<CommonResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.failure(message, "UNAUTHORIZED"));
    }

    // ============================================
    // FORBIDDEN RESPONSES (403)
    // ============================================

    /**
     * Create 403 FORBIDDEN response
     * Usage: return ResponseWrapper.forbidden("Access denied");
     */
    public static <T> ResponseEntity<CommonResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.failure(message, "FORBIDDEN"));
    }

    // ============================================
    // NOT FOUND RESPONSES (404)
    // ============================================

    /**
     * Create 404 NOT FOUND response
     * Usage: return ResponseWrapper.notFound("User not found");
     */
    public static <T> ResponseEntity<CommonResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.failure(message, "NOT_FOUND"));
    }

    // ============================================
    // INTERNAL SERVER ERROR RESPONSES (500)
    // ============================================

    /**
     * Create 500 INTERNAL SERVER ERROR response
     * Usage: return ResponseWrapper.internalError("Server error occurred");
     */
    public static <T> ResponseEntity<CommonResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(message, "INTERNAL_ERROR"));
    }

    /**
     * Create 500 INTERNAL SERVER ERROR response from exception
     * Usage: return ResponseWrapper.fromException(exception);
     */
    public static <T> ResponseEntity<CommonResponse<T>> fromException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fromException(exception));
    }

    /**
     * Create 500 INTERNAL SERVER ERROR response from exception with custom message
     * Usage: return ResponseWrapper.fromException("Failed to process", exception);
     */
    public static <T> ResponseEntity<CommonResponse<T>> fromException(String message, Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fromException(message, exception));
    }

    // ============================================
    // WRAPPER FOR EXISTING RESPONSES
    // ============================================

    /**
     * Wrap existing response into CommonResponse
     * Usage: return ResponseWrapper.wrap(existingResponse);
     */
    public static <T> ResponseEntity<CommonResponse<T>> wrap(T response) {
        return ResponseEntity.ok(CommonResponse.wrap(response));
    }

    /**
     * Wrap existing response with message
     * Usage: return ResponseWrapper.wrap("Success", existingResponse);
     */
    public static <T> ResponseEntity<CommonResponse<T>> wrap(String message, T response) {
        return ResponseEntity.ok(CommonResponse.wrap(message, response));
    }

    // ============================================
    // CUSTOM STATUS CODE
    // ============================================

    /**
     * Create response with custom status code and CommonResponse
     * Usage: return ResponseWrapper.custom(HttpStatus.ACCEPTED,
     * CommonResponse.success(data));
     */
    @SuppressWarnings("null")
    public static <T> ResponseEntity<CommonResponse<T>> custom(HttpStatus status, CommonResponse<T> response) {
        return ResponseEntity.status(status).body(response);
    }
}

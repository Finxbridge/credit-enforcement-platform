package com.finx.auth.exception;

/**
 * Resource Not Found Exception
 * Purpose: Custom exception for resource not found scenarios
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

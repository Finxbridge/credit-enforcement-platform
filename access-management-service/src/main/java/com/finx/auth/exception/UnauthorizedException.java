package com.finx.auth.exception;

/**
 * Unauthorized Exception
 * Purpose: Custom exception for unauthorized access attempts
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

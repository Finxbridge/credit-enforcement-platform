package com.finx.common.exception;

/**
 * Exception thrown when user is not authenticated
 *
 * Usage: throw new UnauthorizedException("Invalid credentials");
 *
 * @author CMS-NMS Team
 * @version 1.0.0
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

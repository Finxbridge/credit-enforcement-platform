package com.finx.communication.exception;

/**
 * Exception thrown when user is authenticated but doesn't have permission
 *
 * Usage: throw new ForbiddenException("You don't have permission to access this
 * resource");
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

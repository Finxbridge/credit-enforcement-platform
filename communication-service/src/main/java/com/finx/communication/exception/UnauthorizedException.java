package com.finx.communication.exception;

/**
 * Exception thrown when user is not authenticated
 *
 * Usage: throw new UnauthorizedException("Invalid credentials");
 *
 * @author Naveen Manyam
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

package com.finx.communication.exception;

/**
 * Exception thrown when external API call fails
 */
public class ApiCallException extends IntegrationException {

    public ApiCallException(String message) {
        super(message);
    }

    public ApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.finx.agencymanagement.exception;

/**
 * Resource Not Found Exception for Agency Management Service
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message, 404);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super("NOT_FOUND", String.format("%s with id '%d' not found", resourceName, id), 404);
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super("NOT_FOUND", String.format("%s with identifier '%s' not found", resourceName, identifier), 404);
    }
}

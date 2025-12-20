package com.finx.communication.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, 404);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s not found with identifier: %s", resource, identifier),
              404);
    }
}

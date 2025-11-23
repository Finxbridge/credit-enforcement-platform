package com.finx.templatemanagementservice.exception;

/**
 * Exception thrown when template already exists
 */
public class TemplateAlreadyExistsException extends RuntimeException {

    public TemplateAlreadyExistsException(String templateCode) {
        super("Template already exists with code: " + templateCode);
    }
}

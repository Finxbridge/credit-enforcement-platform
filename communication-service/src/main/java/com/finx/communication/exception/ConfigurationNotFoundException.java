package com.finx.communication.exception;

/**
 * Exception thrown when integration configuration is not found in cache
 */
public class ConfigurationNotFoundException extends IntegrationException {

    public ConfigurationNotFoundException(String integrationName) {
        super("Configuration not found for integration: " + integrationName);
    }
}

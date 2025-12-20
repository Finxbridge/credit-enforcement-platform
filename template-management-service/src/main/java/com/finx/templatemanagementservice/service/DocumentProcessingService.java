package com.finx.templatemanagementservice.service;

import java.util.Map;

/**
 * Service interface for processing document placeholders
 * Supports PDF and DOC/DOCX formats
 */
public interface DocumentProcessingService {

    /**
     * Process a document and replace placeholders with actual values
     *
     * @param documentUrl the URL/path of the source document
     * @param variables the map of variable names to their resolved values
     * @param caseId the case ID for generating unique output filename
     * @return the URL/path of the processed document
     */
    String processDocument(String documentUrl, Map<String, Object> variables, Long caseId);

    /**
     * Process a document and replace placeholders with actual values
     * This overload includes template metadata for proper document categorization
     *
     * @param documentUrl the URL/path of the source document
     * @param variables the map of variable names to their resolved values
     * @param caseId the case ID for generating unique output filename
     * @param sourceTemplateId the source template ID
     * @param channel the communication channel (SMS, EMAIL, WHATSAPP, NOTICE)
     * @return the URL/path of the processed document
     */
    String processDocument(String documentUrl, Map<String, Object> variables, Long caseId,
                          Long sourceTemplateId, String channel);

    /**
     * Extract placeholders from a document
     *
     * @param documentUrl the URL/path of the document
     * @return list of placeholder names found in the document
     */
    java.util.List<String> extractPlaceholders(String documentUrl);

    /**
     * Check if a document contains any placeholders
     *
     * @param documentUrl the URL/path of the document
     * @return true if the document contains placeholders
     */
    boolean hasPlaceholders(String documentUrl);

    /**
     * Get the document type from the file extension
     *
     * @param filename the filename
     * @return the document type (PDF, DOC, DOCX)
     */
    default String getDocumentType(String filename) {
        if (filename == null) {
            return null;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "PDF";
        } else if (lower.endsWith(".docx")) {
            return "DOCX";
        } else if (lower.endsWith(".doc")) {
            return "DOC";
        }
        return null;
    }
}

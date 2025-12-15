package com.finx.dmsservice.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage Service interface for document storage operations
 * Supports OVH S3-compatible Object Storage
 */
public interface StorageService {

    /**
     * Upload a file to storage
     * @param file the file to upload
     * @param bucket the bucket name
     * @param objectKey the object key (path in bucket)
     * @return the storage URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String bucket, String objectKey);

    /**
     * Upload bytes to storage
     * @param content the file content as bytes
     * @param bucket the bucket name
     * @param objectKey the object key
     * @param contentType the content type
     * @return the storage URL
     */
    String uploadBytes(byte[] content, String bucket, String objectKey, String contentType);

    /**
     * Download a file from storage
     * @param bucket the bucket name
     * @param objectKey the object key
     * @return the file content as bytes
     */
    byte[] downloadFile(String bucket, String objectKey);

    /**
     * Delete a file from storage
     * @param bucket the bucket name
     * @param objectKey the object key
     */
    void deleteFile(String bucket, String objectKey);

    /**
     * Check if a file exists in storage
     * @param bucket the bucket name
     * @param objectKey the object key
     * @return true if file exists
     */
    boolean fileExists(String bucket, String objectKey);

    /**
     * Get a pre-signed URL for temporary access
     * @param bucket the bucket name
     * @param objectKey the object key
     * @param expirationMinutes how long the URL is valid
     * @return the pre-signed URL
     */
    String getPresignedUrl(String bucket, String objectKey, int expirationMinutes);

    /**
     * Get the public URL for a file
     * @param bucket the bucket name
     * @param objectKey the object key
     * @return the public URL
     */
    String getPublicUrl(String bucket, String objectKey);

    /**
     * Get the default bucket for templates
     */
    String getTemplatesBucket();

    /**
     * Get the default bucket for documents
     */
    String getDocumentsBucket();

    /**
     * Get the default bucket for processed documents
     */
    String getProcessedBucket();
}

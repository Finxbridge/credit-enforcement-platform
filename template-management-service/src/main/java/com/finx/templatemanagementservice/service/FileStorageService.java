package com.finx.templatemanagementservice.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file storage operations
 */
public interface FileStorageService {

    /**
     * Upload a file to storage
     * @param file the file to upload
     * @param folder the folder/prefix to store the file
     * @return the URL/path of the stored file
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Download a file from storage
     * @param fileUrl the URL/path of the file
     * @return the file content as byte array
     */
    byte[] downloadFile(String fileUrl);

    /**
     * Delete a file from storage
     * @param fileUrl the URL/path of the file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Check if a file exists
     * @param fileUrl the URL/path of the file
     * @return true if the file exists
     */
    boolean fileExists(String fileUrl);

    /**
     * Get the file extension from a filename
     * @param filename the filename
     * @return the extension (without dot)
     */
    default String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

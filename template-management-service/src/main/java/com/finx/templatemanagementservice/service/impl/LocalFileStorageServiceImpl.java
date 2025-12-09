package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.exception.BusinessException;
import com.finx.templatemanagementservice.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local file system storage implementation
 * For production, consider using S3FileStorageServiceImpl
 */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.storage.base-path:./uploads}")
    private String basePath;

    @Value("${file.storage.base-url:http://localhost:8087/api/v1/files}")
    private String baseUrl;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.info("File storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new BusinessException("Could not initialize file storage: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BusinessException("Cannot upload empty file");
        }

        try {
            // Validate file type
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            if (!isAllowedExtension(extension)) {
                throw new BusinessException("File type not allowed: " + extension);
            }

            // Create folder if not exists
            Path folderPath = rootLocation.resolve(folder);
            Files.createDirectories(folderPath);

            // Generate unique filename
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
            Path targetLocation = folderPath.resolve(uniqueFilename);

            // Copy file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path (used as URL identifier)
            String relativePath = folder + "/" + uniqueFilename;
            log.info("File uploaded successfully: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String fileUrl) {
        try {
            Path filePath = rootLocation.resolve(fileUrl).normalize();

            // Security check - prevent path traversal
            if (!filePath.startsWith(rootLocation)) {
                throw new BusinessException("Access denied: Invalid file path");
            }

            if (!Files.exists(filePath)) {
                throw new BusinessException("File not found: " + fileUrl);
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("Failed to download file: {}", e.getMessage());
            throw new BusinessException("Failed to download file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Path filePath = rootLocation.resolve(fileUrl).normalize();

            // Security check
            if (!filePath.startsWith(rootLocation)) {
                throw new BusinessException("Access denied: Invalid file path");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileUrl);
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new BusinessException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        Path filePath = rootLocation.resolve(fileUrl).normalize();
        return filePath.startsWith(rootLocation) && Files.exists(filePath);
    }

    /**
     * Get the full file path for a given URL
     */
    public Path getFilePath(String fileUrl) {
        return rootLocation.resolve(fileUrl).normalize();
    }

    /**
     * Get the base URL for accessing files
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    private boolean isAllowedExtension(String extension) {
        return extension != null &&
               (extension.equals("pdf") ||
                extension.equals("doc") ||
                extension.equals("docx"));
    }
}

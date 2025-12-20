package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.exception.BusinessException;
import com.finx.templatemanagementservice.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
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
        // Try primary path first
        try {
            rootLocation = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            // Verify we can write to the directory
            if (Files.isWritable(rootLocation)) {
                log.info("File storage initialized at: {}", rootLocation);
                return;
            }
            log.warn("Primary storage path {} is not writable, trying fallback", rootLocation);
        } catch (Exception e) {
            log.warn("Could not initialize file storage at {}: {}", basePath, e.getMessage());
        }

        // Try fallback path in temp directory
        try {
            rootLocation = Paths.get(System.getProperty("java.io.tmpdir"), "template-uploads").toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.warn("Using fallback storage location: {}", rootLocation);
        } catch (Exception e) {
            log.error("Could not initialize fallback file storage: {}", e.getMessage());
            // Last resort: use current working directory
            try {
                rootLocation = Paths.get("uploads").toAbsolutePath().normalize();
                Files.createDirectories(rootLocation);
                log.warn("Using last resort storage location: {}", rootLocation);
            } catch (Exception lastResortException) {
                log.error("All file storage initialization attempts failed. File operations will fail at runtime.");
                rootLocation = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();
            }
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
            // Check if this is an HTTP/HTTPS URL (e.g., from DMS/S3)
            if (fileUrl != null && (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
                return downloadFromUrl(fileUrl);
            }

            // Otherwise, treat as local file path
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

    /**
     * Download file from HTTP/HTTPS URL (e.g., from DMS/S3 storage)
     */
    private byte[] downloadFromUrl(String url) {
        log.info("Downloading file from URL: {}", url);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("File downloaded successfully from URL: {} (size: {} bytes)", url, response.body().length);
                return response.body();
            } else {
                throw new BusinessException("Failed to download file from URL: " + url + " - HTTP " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to download file from URL {}: {}", url, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException("Failed to download file from URL: " + e.getMessage());
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

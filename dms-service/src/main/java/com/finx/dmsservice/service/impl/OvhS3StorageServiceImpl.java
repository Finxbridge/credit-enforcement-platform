package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.exception.BusinessException;
import com.finx.dmsservice.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * OVH S3-compatible Object Storage implementation
 * Loads configuration from third_party_integration_master table
 *
 * Endpoint format: https://s3.<region>.io.cloud.ovh.net
 * UK (London) endpoint: https://s3.uk.io.cloud.ovh.net
 *
 * Reference: https://help.ovhcloud.com/csm/en-public-cloud-storage-s3-location
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OvhS3StorageServiceImpl implements StorageService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    // Configuration loaded from third_party_integration_master
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String templatesBucket;
    private String documentsBucket;
    private String processedBucket;
    private String publicUrlPrefix;

    @PostConstruct
    public void init() {
        loadConfigurationFromDatabase();
        initializeS3Client();
    }

    /**
     * Load OVH S3 configuration from third_party_integration_master table
     */
    private void loadConfigurationFromDatabase() {
        try {
            String sql = """
                SELECT api_endpoint, api_key_encrypted, config_json
                FROM third_party_integration_master
                WHERE integration_name = 'OVH_S3_STORAGE' AND is_active = true
                """;

            jdbcTemplate.query(sql, rs -> {
                endpoint = rs.getString("api_endpoint");
                accessKey = rs.getString("api_key_encrypted"); // Access key stored in api_key_encrypted

                String configJson = rs.getString("config_json");
                if (configJson != null) {
                    try {
                        JsonNode config = objectMapper.readTree(configJson);
                        region = config.path("region").asText("uk");
                        templatesBucket = config.path("bucket_templates").asText("wrathful-de-gennes");
                        documentsBucket = config.path("bucket_documents").asText("wrathful-de-gennes");
                        processedBucket = config.path("bucket_processed").asText("wrathful-de-gennes");
                        publicUrlPrefix = config.path("public_url_prefix").asText("");
                        // Get secret key from config_json if not from environment
                        String configSecretKey = config.path("secret_key").asText(null);
                        if (configSecretKey != null && !configSecretKey.isEmpty()) {
                            secretKey = configSecretKey;
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse OVH S3 config JSON: {}", e.getMessage());
                    }
                }
            });

            // Override with environment variables if present (more secure for production)
            String envAccessKey = System.getenv("OVH_S3_ACCESS_KEY");
            String envSecretKey = System.getenv("OVH_S3_SECRET_KEY");
            if (envAccessKey != null && !envAccessKey.isEmpty()) {
                accessKey = envAccessKey;
            }
            if (envSecretKey != null && !envSecretKey.isEmpty()) {
                secretKey = envSecretKey;
            }

            if (accessKey == null || secretKey == null) {
                log.warn("OVH S3 credentials not found. Storage operations will fail.");
            }

            log.info("OVH S3 configuration loaded - Endpoint: {}, Region: {}, Bucket: {}", endpoint, region, documentsBucket);

        } catch (Exception e) {
            log.error("Failed to load OVH S3 configuration: {}", e.getMessage());
            // Use default values for London (UK) region
            // Endpoint format: https://s3.<region>.io.cloud.ovh.net
            endpoint = "https://s3.uk.io.cloud.ovh.net";
            region = "uk";
            templatesBucket = "wrathful-de-gennes";
            documentsBucket = "wrathful-de-gennes";
            processedBucket = "wrathful-de-gennes";
        }
    }

    /**
     * Initialize S3 client with OVH endpoint
     */
    private void initializeS3Client() {
        if (accessKey == null || secretKey == null) {
            log.warn("Skipping S3 client initialization - credentials not available");
            return;
        }

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            // Build S3 client for OVH (S3v4 signature is default in SDK 2.x)
            // OVHcloud recommends virtual host style with io.cloud.ovh.net endpoints
            // Path style is kept for compatibility - set path_style_access in config_json to control
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .forcePathStyle(true) // Path style for existing bucket wrathful-de-gennes
                    .build();

            // Build S3 presigner for generating pre-signed URLs
            s3Presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .build();

            // Ensure buckets exist
            ensureBucketExists(templatesBucket);
            ensureBucketExists(documentsBucket);
            ensureBucketExists(processedBucket);

            log.info("OVH S3 client initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize OVH S3 client: {}", e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String bucket, String objectKey) {
        validateS3Client();

        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }

        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded to OVH S3: {}/{}", bucket, objectKey);
            return getPublicUrl(bucket, objectKey);

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("S3 error uploading file: {}", e.getMessage());
            throw new BusinessException("S3 error: " + e.getMessage());
        }
    }

    @Override
    public String uploadBytes(byte[] content, String bucket, String objectKey, String contentType) {
        validateS3Client();

        if (content == null || content.length == 0) {
            throw new BusinessException("Content is required");
        }

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .contentLength((long) content.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(content));

            log.info("Bytes uploaded to OVH S3: {}/{}", bucket, objectKey);
            return getPublicUrl(bucket, objectKey);

        } catch (S3Exception e) {
            log.error("S3 error uploading bytes: {}", e.getMessage());
            throw new BusinessException("S3 error: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String bucket, String objectKey) {
        validateS3Client();

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            byte[] content = s3Client.getObjectAsBytes(getRequest).asByteArray();
            log.debug("File downloaded from OVH S3: {}/{}", bucket, objectKey);
            return content;

        } catch (NoSuchKeyException e) {
            log.error("File not found: {}/{}", bucket, objectKey);
            throw new BusinessException("File not found: " + objectKey);
        } catch (S3Exception e) {
            log.error("S3 error downloading file: {}", e.getMessage());
            throw new BusinessException("Failed to download file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String bucket, String objectKey) {
        validateS3Client();

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from OVH S3: {}/{}", bucket, objectKey);

        } catch (S3Exception e) {
            log.error("S3 error deleting file: {}", e.getMessage());
            throw new BusinessException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String bucket, String objectKey) {
        if (s3Client == null) {
            return false;
        }

        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String objectKey, int expirationMinutes) {
        if (s3Presigner == null) {
            return getPublicUrl(bucket, objectKey);
        }

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage());
            return getPublicUrl(bucket, objectKey);
        }
    }

    @Override
    public String getPublicUrl(String bucket, String objectKey) {
        if (publicUrlPrefix != null && !publicUrlPrefix.isEmpty()) {
            return publicUrlPrefix + "/" + bucket + "/" + objectKey;
        }
        return endpoint + "/" + bucket + "/" + objectKey;
    }

    @Override
    public String getTemplatesBucket() {
        return templatesBucket;
    }

    @Override
    public String getDocumentsBucket() {
        return documentsBucket;
    }

    @Override
    public String getProcessedBucket() {
        return processedBucket;
    }

    private void validateS3Client() {
        if (s3Client == null) {
            throw new BusinessException("S3 client not initialized. Check OVH credentials.");
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            HeadBucketRequest headRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headRequest);
            log.debug("Bucket exists: {}", bucketName);

        } catch (NoSuchBucketException e) {
            log.info("Creating bucket: {}", bucketName);
            try {
                CreateBucketRequest createRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createRequest);
                log.info("Bucket created: {}", bucketName);
            } catch (S3Exception createEx) {
                log.warn("Could not create bucket {}: {}", bucketName, createEx.getMessage());
            }
        } catch (S3Exception e) {
            log.warn("Could not check bucket {}: {}", bucketName, e.getMessage());
        }
    }
}

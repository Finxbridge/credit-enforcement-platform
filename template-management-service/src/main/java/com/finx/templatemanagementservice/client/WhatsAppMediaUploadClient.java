package com.finx.templatemanagementservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.templatemanagementservice.domain.dto.CommonResponse;
import com.finx.templatemanagementservice.domain.dto.comm.WhatsAppMediaUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST Client for uploading media files to communication-service for WhatsApp templates
 * Uses RestTemplate instead of Feign because Feign doesn't handle multipart well
 */
@Slf4j
@Component
public class WhatsAppMediaUploadClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${COMMUNICATION_SERVICE_URL:http://localhost:8085}")
    private String communicationServiceUrl;

    public WhatsAppMediaUploadClient(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * Upload media file to communication-service for WhatsApp template creation
     *
     * @param file The document file (PDF, DOC, DOCX, JPG, PNG, MP4)
     * @return Response containing the header_handle URL from MSG91
     */
    public CommonResponse<WhatsAppMediaUploadResponse> uploadMedia(MultipartFile file) {
        log.info("Uploading media to communication-service: filename={}, size={}, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            String url = communicationServiceUrl + "/comm/whatsapp/media-upload";

            // Build multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call communication-service
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Media upload response: status={}, body={}", response.getStatusCode(), response.getBody());

            // Parse response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // The response is wrapped in CommonResponse
                CommonResponse<WhatsAppMediaUploadResponse> commonResponse = objectMapper.readValue(
                        response.getBody(),
                        objectMapper.getTypeFactory().constructParametricType(
                                CommonResponse.class, WhatsAppMediaUploadResponse.class));
                return commonResponse;
            }

            return CommonResponse.<WhatsAppMediaUploadResponse>builder()
                    .status("failure")
                    .message("Failed to upload media: " + response.getStatusCode())
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload media", e);
            return CommonResponse.<WhatsAppMediaUploadResponse>builder()
                    .status("failure")
                    .message("Failed to upload media: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Upload media file bytes to communication-service for WhatsApp template creation
     *
     * @param fileBytes The file bytes
     * @param filename The original filename
     * @param contentType The content type (application/pdf, image/jpeg, etc.)
     * @return Response containing the header_handle URL from MSG91
     */
    public CommonResponse<WhatsAppMediaUploadResponse> uploadMedia(byte[] fileBytes, String filename, String contentType) {
        log.info("Uploading media bytes to communication-service: filename={}, size={}, contentType={}",
                filename, fileBytes.length, contentType);

        try {
            String url = communicationServiceUrl + "/comm/whatsapp/media-upload";

            // Build multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call communication-service
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Media upload response: status={}, body={}", response.getStatusCode(), response.getBody());

            // Parse response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CommonResponse<WhatsAppMediaUploadResponse> commonResponse = objectMapper.readValue(
                        response.getBody(),
                        objectMapper.getTypeFactory().constructParametricType(
                                CommonResponse.class, WhatsAppMediaUploadResponse.class));
                return commonResponse;
            }

            return CommonResponse.<WhatsAppMediaUploadResponse>builder()
                    .status("failure")
                    .message("Failed to upload media: " + response.getStatusCode())
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload media", e);
            return CommonResponse.<WhatsAppMediaUploadResponse>builder()
                    .status("failure")
                    .message("Failed to upload media: " + e.getMessage())
                    .build();
        }
    }
}

package com.finx.strategyengineservice.client;

import com.finx.strategyengineservice.client.dto.DmsDocumentDTO;
import com.finx.strategyengineservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for DMS Service
 * Used to fetch documents from OVH S3 storage
 * Strategy Engine only needs GET operations - upload is done via template-management-service
 */
@FeignClient(name = "dms-service", url = "${DMS_SERVICE_URL:http://localhost:8093}")
public interface DmsServiceClient {

    /**
     * Get document metadata by ID
     */
    @GetMapping("/documents/{id}")
    CommonResponse<DmsDocumentDTO> getDocumentById(@PathVariable("id") Long id);

    /**
     * Get document metadata by document ID (string)
     */
    @GetMapping("/documents/document-id/{documentId}")
    CommonResponse<DmsDocumentDTO> getDocumentByDocumentId(@PathVariable("documentId") String documentId);

    /**
     * Get document content as bytes
     */
    @GetMapping("/documents/{id}/content")
    byte[] getDocumentContent(@PathVariable("id") Long id, @RequestParam(value = "userId", required = false) Long userId);

    /**
     * Get document content by document ID (string)
     */
    @GetMapping("/documents/by-document-id/{documentId}/content")
    byte[] getDocumentContentByDocumentId(
            @PathVariable("documentId") String documentId,
            @RequestParam(value = "userId", required = false) Long userId);

    /**
     * Get presigned URL for direct S3 access
     */
    @GetMapping("/documents/{id}/presigned-url")
    CommonResponse<String> getPresignedUrl(
            @PathVariable("id") Long id,
            @RequestParam(value = "expirationMinutes", defaultValue = "60") int expirationMinutes);
}

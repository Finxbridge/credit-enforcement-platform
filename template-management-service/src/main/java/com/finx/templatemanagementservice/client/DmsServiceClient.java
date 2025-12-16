package com.finx.templatemanagementservice.client;

import com.finx.templatemanagementservice.client.dto.DmsDocumentDTO;
import com.finx.templatemanagementservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Feign Client for DMS Service
 * Used to upload/download documents to OVH S3 storage
 */
@FeignClient(name = "dms-service", url = "${DMS_SERVICE_URL:http://localhost:8093}")
public interface DmsServiceClient {

        /**
         * Upload document to DMS
         *
         * @param file         Required - The file to upload
         * @param documentName Optional - Custom name (defaults to original filename)
         */
        @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        CommonResponse<DmsDocumentDTO> uploadDocument(
                        @RequestPart("file") MultipartFile file,
                        @RequestParam(value = "documentName", required = false) String documentName);

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
        byte[] getDocumentContent(@PathVariable("id") Long id,
                        @RequestParam(value = "userId", required = false) Long userId);

        /**
         * Get presigned URL for direct S3 access (valid 60 min)
         */
        @GetMapping("/documents/{id}/url")
        CommonResponse<String> getPresignedUrl(@PathVariable("id") Long id);

        /**
         * Delete document
         */
        @DeleteMapping("/documents/{id}")
        CommonResponse<Void> deleteDocument(@PathVariable("id") Long id);

        /**
         * Permanently delete document (also removes from S3)
         */
        @DeleteMapping("/documents/{id}/permanent")
        CommonResponse<Void> permanentlyDeleteDocument(@PathVariable("id") Long id);
}

package com.finx.collectionsservice.client;

import com.finx.collectionsservice.client.dto.CommonResponse;
import com.finx.collectionsservice.client.dto.DmsDocumentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Feign Client for DMS Service
 * Used to upload/download documents to OVH S3 storage
 */
@FeignClient(name = "dms-service", url = "${DMS_SERVICE_URL:http://localhost:8093}")
public interface DmsServiceClient {

    /**
     * Upload document to DMS with category and channel
     *
     * @param file             Required - The file to upload
     * @param documentName     Optional - Custom name
     * @param documentCategory Optional - Category: TEMPLATE, GENERATED, USER_UPLOAD
     * @param channel          Optional - Channel: SMS, EMAIL, WHATSAPP, NOTICE, SETTLEMENT_LETTER
     * @param caseId           Optional - Case ID for GENERATED documents
     * @param sourceTemplateId Optional - Source template ID for GENERATED documents
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CommonResponse<DmsDocumentDTO> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "documentCategory", required = false) String documentCategory,
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "caseId", required = false) Long caseId,
            @RequestParam(value = "sourceTemplateId", required = false) Long sourceTemplateId);

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
     * Get documents by case ID
     */
    @GetMapping("/documents/case/{caseId}")
    CommonResponse<List<DmsDocumentDTO>> getDocumentsByCaseId(@PathVariable("caseId") Long caseId);

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
     * Delete document (soft delete)
     */
    @DeleteMapping("/documents/{id}")
    CommonResponse<Void> deleteDocument(@PathVariable("id") Long id);
}

package com.finx.dmsservice.controller;

import com.finx.dmsservice.domain.dto.CommonResponse;
import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import com.finx.dmsservice.domain.enums.ChannelType;
import com.finx.dmsservice.domain.enums.DocumentCategory;
import com.finx.dmsservice.service.DocumentService;
import com.finx.dmsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Simple Document Controller
 * Provides upload, download, and retrieval operations for documents stored in OVH S3
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // ==================== UPLOAD API ====================

    /**
     * Simple document upload endpoint
     *
     * @param file              Required - The file to upload
     * @param documentName      Optional - Custom name (defaults to original filename)
     * @param documentCategory  Optional - Category: TEMPLATE, GENERATED, USER_UPLOAD (default: USER_UPLOAD)
     * @param channel           Optional - Channel: SMS, EMAIL, WHATSAPP, NOTICE
     * @param caseId            Optional - Case ID for GENERATED documents
     * @param sourceTemplateId  Optional - Source template ID for GENERATED documents
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<DocumentDTO>> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "documentCategory", required = false) String documentCategory,
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "caseId", required = false) Long caseId,
            @RequestParam(value = "sourceTemplateId", required = false) Long sourceTemplateId) {

        UploadDocumentRequest request = UploadDocumentRequest.builder()
                .documentName(documentName != null ? documentName : file.getOriginalFilename())
                .documentCategory(documentCategory)
                .channel(channel)
                .caseId(caseId)
                .sourceTemplateId(sourceTemplateId)
                .build();

        DocumentDTO document = documentService.uploadDocument(request, file);
        return ResponseWrapper.created("Document uploaded successfully", document);
    }

    // ==================== RETRIEVAL APIs ====================

    /**
     * Get document by internal ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DocumentDTO>> getDocumentById(@PathVariable Long id) {
        DocumentDTO document = documentService.getDocumentById(id);
        return ResponseWrapper.ok("Document retrieved successfully", document);
    }

    /**
     * Get document by document ID (string reference)
     */
    @GetMapping("/document-id/{documentId}")
    public ResponseEntity<CommonResponse<DocumentDTO>> getDocumentByDocumentId(@PathVariable String documentId) {
        DocumentDTO document = documentService.getDocumentByDocumentId(documentId);
        return ResponseWrapper.ok("Document retrieved successfully", document);
    }

    /**
     * Get all documents with pagination and optional filters
     *
     * @param category Optional - Filter by category: TEMPLATE, GENERATED, USER_UPLOAD
     * @param channel  Optional - Filter by channel: SMS, EMAIL, WHATSAPP, NOTICE
     * @param pageable Pagination parameters
     *
     * Examples:
     *   GET /documents                                    - All documents
     *   GET /documents?category=TEMPLATE                  - All non-generated (template) documents
     *   GET /documents?category=GENERATED                 - All generated documents
     *   GET /documents?category=TEMPLATE&channel=NOTICE   - Template documents for NOTICE channel
     *   GET /documents?category=GENERATED&channel=NOTICE  - Generated documents for NOTICE channel
     */
    @GetMapping
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getAllDocuments(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "channel", required = false) String channel,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<DocumentDTO> documents;

        // Parse category and channel if provided
        DocumentCategory parsedCategory = parseCategory(category);
        ChannelType parsedChannel = parseChannel(channel);

        if (parsedCategory != null && parsedChannel != null) {
            // Filter by both category and channel
            documents = documentService.getDocumentsByCategoryAndChannel(parsedCategory, parsedChannel, pageable);
        } else if (parsedCategory != null) {
            // Filter by category only
            documents = documentService.getDocumentsByCategory(parsedCategory, pageable);
        } else if (parsedChannel != null) {
            // Filter by channel only
            documents = documentService.getDocumentsByChannel(parsedChannel, pageable);
        } else {
            // No filters - return all
            documents = documentService.getAllDocuments(pageable);
        }

        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    /**
     * Get documents by case ID
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<CommonResponse<List<DocumentDTO>>> getDocumentsByCaseId(@PathVariable Long caseId) {
        List<DocumentDTO> documents = documentService.getDocumentsByCaseId(caseId);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    private DocumentCategory parseCategory(String category) {
        if (category == null || category.isEmpty()) {
            return null;
        }
        try {
            return DocumentCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ChannelType parseChannel(String channel) {
        if (channel == null || channel.isEmpty()) {
            return null;
        }
        try {
            return ChannelType.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ==================== DOWNLOAD APIs ====================

    /**
     * Get document content as raw bytes with proper content type
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> getDocumentContent(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        DocumentDTO doc = documentService.getDocumentById(id);
        byte[] content = documentService.downloadDocument(id, userId, "internal");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(doc.getFileType() != null ? doc.getFileType() : "application/octet-stream"));
        headers.setContentDispositionFormData("attachment", doc.getFileName());
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    /**
     * Get document content by document ID (string reference)
     */
    @GetMapping("/by-document-id/{documentId}/content")
    public ResponseEntity<byte[]> getDocumentContentByDocumentId(
            @PathVariable String documentId,
            @RequestParam(required = false) Long userId) {
        DocumentDTO doc = documentService.getDocumentByDocumentId(documentId);
        byte[] content = documentService.downloadDocument(doc.getId(), userId, "internal");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(doc.getFileType() != null ? doc.getFileType() : "application/octet-stream"));
        headers.setContentDispositionFormData("attachment", doc.getFileName());
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    /**
     * Get document URL (presigned URL for direct S3 access)
     */
    @GetMapping("/{id}/url")
    public ResponseEntity<CommonResponse<String>> getDocumentUrl(@PathVariable Long id) {
        String url = documentService.getDocumentUrl(id);
        return ResponseWrapper.ok("Document URL retrieved successfully", url);
    }

    // ==================== UPDATE & DELETE APIs ====================

    /**
     * Update document metadata
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<DocumentDTO>> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody UploadDocumentRequest request) {
        DocumentDTO document = documentService.updateDocument(id, request);
        return ResponseWrapper.ok("Document updated successfully", document);
    }

    /**
     * Soft delete document
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseWrapper.okMessage("Document deleted successfully");
    }

    /**
     * Permanently delete document (removes from S3)
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<CommonResponse<Void>> permanentlyDeleteDocument(@PathVariable Long id) {
        documentService.permanentlyDeleteDocument(id);
        return ResponseWrapper.okMessage("Document permanently deleted");
    }
}

package com.finx.dmsservice.controller;

import com.finx.dmsservice.domain.dto.CommonResponse;
import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import com.finx.dmsservice.service.DocumentService;
import com.finx.dmsservice.util.ResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<CommonResponse<DocumentDTO>> uploadDocument(
            @Valid @RequestPart("request") UploadDocumentRequest request,
            @RequestPart("file") MultipartFile file) {
        DocumentDTO document = documentService.uploadDocument(request, file);
        return ResponseWrapper.created("Document uploaded successfully", document);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DocumentDTO>> getDocumentById(@PathVariable Long id) {
        DocumentDTO document = documentService.getDocumentById(id);
        return ResponseWrapper.ok("Document retrieved successfully", document);
    }

    @GetMapping("/document-id/{documentId}")
    public ResponseEntity<CommonResponse<DocumentDTO>> getDocumentByDocumentId(@PathVariable String documentId) {
        DocumentDTO document = documentService.getDocumentByDocumentId(documentId);
        return ResponseWrapper.ok("Document retrieved successfully", document);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<CommonResponse<List<DocumentDTO>>> getDocumentsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        List<DocumentDTO> documents = documentService.getDocumentsByEntity(entityType, entityId);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getDocumentsByType(
            @PathVariable DocumentType type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDTO> documents = documentService.getDocumentsByType(type, pageable);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getDocumentsByStatus(
            @PathVariable DocumentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDTO> documents = documentService.getDocumentsByStatus(status, pageable);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getDocumentsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDTO> documents = documentService.getDocumentsByCategory(categoryId, pageable);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    @GetMapping("/date-range")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getDocumentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDTO> documents = documentService.getDocumentsByDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getAllDocuments(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDTO> documents = documentService.getAllDocuments(pageable);
        return ResponseWrapper.ok("Documents retrieved successfully", documents);
    }

    @GetMapping("/archived")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> getArchivedDocuments(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDTO> documents = documentService.getArchivedDocuments(pageable);
        return ResponseWrapper.ok("Archived documents retrieved successfully", documents);
    }

    @GetMapping("/{documentId}/versions")
    public ResponseEntity<CommonResponse<List<DocumentDTO>>> getDocumentVersions(@PathVariable Long documentId) {
        List<DocumentDTO> versions = documentService.getDocumentVersions(documentId);
        return ResponseWrapper.ok("Document versions retrieved successfully", versions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<DocumentDTO>> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody UploadDocumentRequest request) {
        DocumentDTO document = documentService.updateDocument(id, request);
        return ResponseWrapper.ok("Document updated successfully", document);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<CommonResponse<DocumentDTO>> archiveDocument(
            @PathVariable Long id,
            @RequestParam Long archivedBy) {
        DocumentDTO document = documentService.archiveDocument(id, archivedBy);
        return ResponseWrapper.ok("Document archived successfully", document);
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<CommonResponse<DocumentDTO>> restoreDocument(@PathVariable Long id) {
        DocumentDTO document = documentService.restoreDocument(id);
        return ResponseWrapper.ok("Document restored successfully", document);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseWrapper.okMessage("Document deleted successfully");
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<CommonResponse<Void>> permanentlyDeleteDocument(@PathVariable Long id) {
        documentService.permanentlyDeleteDocument(id);
        return ResponseWrapper.okMessage("Document permanently deleted");
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<CommonResponse<byte[]>> downloadDocument(
            @PathVariable Long id,
            @RequestParam Long userId,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        byte[] content = documentService.downloadDocument(id, userId, ipAddress);
        return ResponseWrapper.ok("Document downloaded successfully", content);
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<CommonResponse<String>> getDocumentUrl(@PathVariable Long id) {
        String url = documentService.getDocumentUrl(id);
        return ResponseWrapper.ok("Document URL retrieved successfully", url);
    }

    @GetMapping("/count/{entityType}/{entityId}")
    public ResponseEntity<CommonResponse<Long>> countByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        Long count = documentService.countByEntity(entityType, entityId);
        return ResponseWrapper.ok("Document count retrieved successfully", count);
    }

    /**
     * Download document as raw bytes with proper content type
     * Used by other services to fetch documents directly
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
     * Get presigned URL for direct S3 access
     * Used by other services for time-limited access to documents
     */
    @GetMapping("/{id}/presigned-url")
    public ResponseEntity<CommonResponse<String>> getPresignedUrl(
            @PathVariable Long id,
            @RequestParam(defaultValue = "60") int expirationMinutes) {
        String url = documentService.getDocumentUrl(id);
        return ResponseWrapper.ok("Presigned URL generated successfully", url);
    }

    /**
     * Get document by document ID (string) - useful for external references
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
}

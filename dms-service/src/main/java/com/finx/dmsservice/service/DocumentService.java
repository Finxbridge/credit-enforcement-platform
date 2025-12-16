package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Simple Document Service Interface
 * Provides basic document storage operations using OVH S3
 */
public interface DocumentService {

    DocumentDTO uploadDocument(UploadDocumentRequest request, MultipartFile file);

    DocumentDTO getDocumentById(Long id);

    DocumentDTO getDocumentByDocumentId(String documentId);

    Page<DocumentDTO> getAllDocuments(Pageable pageable);

    DocumentDTO updateDocument(Long id, UploadDocumentRequest request);

    void deleteDocument(Long id);

    void permanentlyDeleteDocument(Long id);

    byte[] downloadDocument(Long id, Long userId, String ipAddress);

    String getDocumentUrl(Long id);
}

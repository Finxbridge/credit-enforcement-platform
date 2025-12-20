package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import com.finx.dmsservice.domain.enums.ChannelType;
import com.finx.dmsservice.domain.enums.DocumentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Simple Document Service Interface
 * Provides basic document storage operations using OVH S3
 */
public interface DocumentService {

    DocumentDTO uploadDocument(UploadDocumentRequest request, MultipartFile file);

    DocumentDTO getDocumentById(Long id);

    DocumentDTO getDocumentByDocumentId(String documentId);

    Page<DocumentDTO> getAllDocuments(Pageable pageable);

    // New methods for filtering by category and channel
    Page<DocumentDTO> getDocumentsByCategory(DocumentCategory category, Pageable pageable);

    Page<DocumentDTO> getDocumentsByChannel(ChannelType channel, Pageable pageable);

    Page<DocumentDTO> getDocumentsByCategoryAndChannel(DocumentCategory category, ChannelType channel, Pageable pageable);

    List<DocumentDTO> getDocumentsByCaseId(Long caseId);

    DocumentDTO updateDocument(Long id, UploadDocumentRequest request);

    void deleteDocument(Long id);

    void permanentlyDeleteDocument(Long id);

    byte[] downloadDocument(Long id, Long userId, String ipAddress);

    String getDocumentUrl(Long id);
}

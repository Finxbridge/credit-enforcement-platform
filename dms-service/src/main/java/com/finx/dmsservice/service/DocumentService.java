package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentService {

    DocumentDTO uploadDocument(UploadDocumentRequest request, MultipartFile file);

    DocumentDTO getDocumentById(Long id);

    DocumentDTO getDocumentByDocumentId(String documentId);

    List<DocumentDTO> getDocumentsByEntity(EntityType entityType, Long entityId);

    Page<DocumentDTO> getDocumentsByType(DocumentType type, Pageable pageable);

    Page<DocumentDTO> getDocumentsByStatus(DocumentStatus status, Pageable pageable);

    Page<DocumentDTO> getDocumentsByCategory(Long categoryId, Pageable pageable);

    Page<DocumentDTO> getDocumentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<DocumentDTO> getAllDocuments(Pageable pageable);

    Page<DocumentDTO> getArchivedDocuments(Pageable pageable);

    List<DocumentDTO> getDocumentVersions(Long documentId);

    DocumentDTO updateDocument(Long id, UploadDocumentRequest request);

    DocumentDTO archiveDocument(Long id, Long archivedBy);

    DocumentDTO restoreDocument(Long id);

    void deleteDocument(Long id);

    void permanentlyDeleteDocument(Long id);

    byte[] downloadDocument(Long id, Long userId, String ipAddress);

    String getDocumentUrl(Long id);

    Long countByEntity(EntityType entityType, Long entityId);
}

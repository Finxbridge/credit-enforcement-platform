package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.config.CacheConstants;
import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.domain.entity.DocumentAccessLog;
import com.finx.dmsservice.domain.enums.AccessType;
import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import com.finx.dmsservice.domain.enums.StorageProvider;
import com.finx.dmsservice.exception.BusinessException;
import com.finx.dmsservice.exception.ResourceNotFoundException;
import com.finx.dmsservice.mapper.DocumentMapper;
import com.finx.dmsservice.repository.DocumentAccessLogRepository;
import com.finx.dmsservice.repository.DocumentCategoryRepository;
import com.finx.dmsservice.repository.DocumentRepository;
import com.finx.dmsservice.service.DocumentService;
import com.finx.dmsservice.service.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final DocumentMapper documentMapper;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;

    private String toJson(Map<String, Object> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize map to JSON", e);
            return null;
        }
    }

    private String toJson(List<String> list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize list to JSON", e);
            return null;
        }
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE, CacheConstants.DOCUMENT_BY_ENTITY_CACHE}, allEntries = true)
    public DocumentDTO uploadDocument(UploadDocumentRequest request, MultipartFile file) {
        log.info("Uploading document for entity: {} - {}", request.getEntityType(), request.getEntityId());

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }

        // Generate document ID
        String documentId = generateDocumentId();

        // Determine storage bucket based on entity type or use default documents bucket
        String bucket = determineStorageBucket(request.getEntityType());
        String storagePath = buildStoragePath(request.getEntityType(), request.getEntityId(), documentId, file.getOriginalFilename());

        // Upload to OVH S3 storage
        String fileUrl = storageService.uploadFile(file, bucket, storagePath);
        log.info("File uploaded to OVH S3: {}", fileUrl);

        Document document = Document.builder()
                .documentId(documentId)
                .documentType(request.getDocumentType())
                .documentSubtype(request.getDocumentSubtype())
                .categoryId(request.getCategoryId())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .documentName(request.getDocumentName())
                .description(request.getDescription())
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .fileHash(calculateFileHash(file))
                .storageProvider(StorageProvider.S3)
                .storagePath(storagePath)
                .storageBucket(bucket)
                .metadata(toJson(request.getMetadata()))
                .tags(toJson(request.getTags()))
                .documentStatus(DocumentStatus.ACTIVE)
                .isArchived(false)
                .versionNumber(1)
                .retentionDays(request.getRetentionDays())
                .createdBy(request.getCreatedBy())
                .build();

        if (request.getRetentionDays() != null) {
            document.setExpiresAt(LocalDateTime.now().plusDays(request.getRetentionDays()));
        }

        Document savedDocument = documentRepository.save(document);
        log.info("Document uploaded with ID: {}", savedDocument.getDocumentId());

        return enrichWithCategoryName(documentMapper.toDto(savedDocument));
    }

    @Override
    @Cacheable(value = CacheConstants.DOCUMENT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        return enrichWithCategoryName(documentMapper.toDto(document));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDTO getDocumentByDocumentId(String documentId) {
        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return enrichWithCategoryName(documentMapper.toDto(document));
    }

    @Override
    @Cacheable(value = CacheConstants.DOCUMENT_BY_ENTITY_CACHE, key = "#entityType + '_' + #entityId")
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByEntity(EntityType entityType, Long entityId) {
        return documentRepository.findActiveDocumentsByEntity(entityType, entityId).stream()
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByType(DocumentType type, Pageable pageable) {
        return documentRepository.findByDocumentType(type, pageable)
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByStatus(DocumentStatus status, Pageable pageable) {
        return documentRepository.findByDocumentStatus(status, pageable)
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByCategory(Long categoryId, Pageable pageable) {
        return documentRepository.findByCategoryId(categoryId, pageable)
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return documentRepository.findByDateRange(startDate, endDate, pageable)
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getArchivedDocuments(Pageable pageable) {
        return documentRepository.findArchivedDocuments(pageable)
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentVersions(Long documentId) {
        return documentRepository.findVersionsByParentId(documentId).stream()
                .map(documentMapper::toDto)
                .map(this::enrichWithCategoryName)
                .toList();
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE, CacheConstants.DOCUMENT_BY_ENTITY_CACHE}, allEntries = true)
    public DocumentDTO updateDocument(Long id, UploadDocumentRequest request) {
        log.info("Updating document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setDocumentName(request.getDocumentName());
        document.setDescription(request.getDescription());
        document.setMetadata(toJson(request.getMetadata()));
        document.setTags(toJson(request.getTags()));
        document.setCategoryId(request.getCategoryId());

        Document savedDocument = documentRepository.save(document);
        return enrichWithCategoryName(documentMapper.toDto(savedDocument));
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE, CacheConstants.DOCUMENT_BY_ENTITY_CACHE}, allEntries = true)
    public DocumentDTO archiveDocument(Long id, Long archivedBy) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setIsArchived(true);
        document.setArchivedAt(LocalDateTime.now());
        document.setArchivedBy(archivedBy);
        document.setDocumentStatus(DocumentStatus.ARCHIVED);

        return enrichWithCategoryName(documentMapper.toDto(documentRepository.save(document)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE, CacheConstants.DOCUMENT_BY_ENTITY_CACHE}, allEntries = true)
    public DocumentDTO restoreDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setIsArchived(false);
        document.setArchivedAt(null);
        document.setArchivedBy(null);
        document.setDocumentStatus(DocumentStatus.ACTIVE);

        return enrichWithCategoryName(documentMapper.toDto(documentRepository.save(document)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE, CacheConstants.DOCUMENT_BY_ENTITY_CACHE}, allEntries = true)
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setDocumentStatus(DocumentStatus.DELETED);
        documentRepository.save(document);
        log.info("Document soft deleted: {}", id);
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE, CacheConstants.DOCUMENT_BY_ENTITY_CACHE}, allEntries = true)
    public void permanentlyDeleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        // Delete from OVH S3 storage
        if (document.getStorageBucket() != null && document.getStoragePath() != null) {
            try {
                storageService.deleteFile(document.getStorageBucket(), document.getStoragePath());
                log.info("File deleted from S3: {}/{}", document.getStorageBucket(), document.getStoragePath());
            } catch (Exception e) {
                log.warn("Failed to delete file from S3: {}", e.getMessage());
            }
        }

        documentRepository.deleteById(id);
        log.info("Document permanently deleted: {}", id);
    }

    @Override
    public byte[] downloadDocument(Long id, Long userId, String ipAddress) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        // Log access
        DocumentAccessLog accessLog = DocumentAccessLog.builder()
                .documentId(id)
                .userId(userId)
                .accessType(AccessType.DOWNLOAD)
                .accessIp(ipAddress)
                .accessedAt(LocalDateTime.now())
                .build();
        accessLogRepository.save(accessLog);

        // Download from OVH S3 storage
        log.info("Document download requested: {} by user: {}", id, userId);
        return storageService.downloadFile(document.getStorageBucket(), document.getStoragePath());
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentUrl(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        // Return presigned URL for secure access (valid for 60 minutes)
        return storageService.getPresignedUrl(document.getStorageBucket(), document.getStoragePath(), 60);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByEntity(EntityType entityType, Long entityId) {
        return documentRepository.countByEntity(entityType, entityId);
    }

    private String generateDocumentId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "DOC-" + datePart + "-" + uniquePart;
    }

    private String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("Could not calculate file hash", e);
            return null;
        }
    }

    private DocumentDTO enrichWithCategoryName(DocumentDTO dto) {
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getCategoryName()));
        }
        return dto;
    }

    /**
     * Determine the storage bucket based on entity type
     */
    private String determineStorageBucket(EntityType entityType) {
        if (entityType == null) {
            return storageService.getDocumentsBucket();
        }
        return switch (entityType) {
            case TEMPLATE -> storageService.getTemplatesBucket();
            case CASE, CUSTOMER, LOAN, ACCOUNT -> storageService.getDocumentsBucket();
            default -> storageService.getDocumentsBucket();
        };
    }

    /**
     * Build a structured storage path for the document
     * Format: {entityType}/{entityId}/{documentId}/{filename}
     */
    private String buildStoragePath(EntityType entityType, Long entityId, String documentId, String filename) {
        String entityFolder = entityType != null ? entityType.name().toLowerCase() : "general";
        String entityIdFolder = entityId != null ? entityId.toString() : "unassigned";
        return String.format("%s/%s/%s/%s", entityFolder, entityIdFolder, documentId, filename);
    }
}

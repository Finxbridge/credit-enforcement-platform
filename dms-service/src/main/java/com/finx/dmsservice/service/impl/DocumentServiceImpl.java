package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.config.CacheConstants;
import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.UploadDocumentRequest;
import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.domain.entity.DocumentAccessLog;
import com.finx.dmsservice.domain.enums.AccessType;
import com.finx.dmsservice.domain.enums.ChannelType;
import com.finx.dmsservice.domain.enums.DocumentCategory;
import com.finx.dmsservice.exception.BusinessException;
import com.finx.dmsservice.exception.ResourceNotFoundException;
import com.finx.dmsservice.mapper.DocumentMapper;
import com.finx.dmsservice.repository.DocumentAccessLogRepository;
import com.finx.dmsservice.repository.DocumentRepository;
import com.finx.dmsservice.service.DocumentService;
import com.finx.dmsservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Simple Document Service Implementation
 * Handles document upload, retrieval, and deletion using OVH S3 storage
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final DocumentMapper documentMapper;
    private final StorageService storageService;

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE}, allEntries = true)
    public DocumentDTO uploadDocument(UploadDocumentRequest request, MultipartFile file) {
        log.info("Uploading document: {}", request.getDocumentName());

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }

        // Generate document ID
        String documentId = generateDocumentId();

        // Parse category and channel from request
        DocumentCategory category = parseCategory(request.getDocumentCategory());
        ChannelType channel = parseChannel(request.getChannel());

        // Use default documents bucket
        String bucket = storageService.getDocumentsBucket();
        String storagePath = buildStoragePath(documentId, file.getOriginalFilename(), category, channel);

        // Upload to OVH S3 storage
        String fileUrl = storageService.uploadFile(file, bucket, storagePath);
        log.info("File uploaded to OVH S3: {}", fileUrl);

        Document document = Document.builder()
                .documentId(documentId)
                .documentName(request.getDocumentName())
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .storagePath(storagePath)
                .storageBucket(bucket)
                .documentStatus("ACTIVE")
                .documentCategory(category)
                .channel(channel)
                .caseId(request.getCaseId())
                .sourceTemplateId(request.getSourceTemplateId())
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("Document uploaded with ID: {}, category: {}, channel: {}",
                savedDocument.getDocumentId(), category, channel);

        return documentMapper.toDto(savedDocument);
    }

    @Override
    @Cacheable(value = CacheConstants.DOCUMENT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDTO getDocumentByDocumentId(String documentId) {
        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByCategory(DocumentCategory category, Pageable pageable) {
        return documentRepository.findByDocumentCategory(category, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByChannel(ChannelType channel, Pageable pageable) {
        return documentRepository.findByChannel(channel, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByCategoryAndChannel(DocumentCategory category, ChannelType channel, Pageable pageable) {
        return documentRepository.findByDocumentCategoryAndChannel(category, channel, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByCaseId(Long caseId) {
        return documentRepository.findByCaseId(caseId).stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE}, allEntries = true)
    public DocumentDTO updateDocument(Long id, UploadDocumentRequest request) {
        log.info("Updating document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setDocumentName(request.getDocumentName());

        Document savedDocument = documentRepository.save(document);
        return documentMapper.toDto(savedDocument);
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE}, allEntries = true)
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setDocumentStatus("DELETED");
        documentRepository.save(document);
        log.info("Document soft deleted: {}", id);
    }

    @Override
    @CacheEvict(value = {CacheConstants.DOCUMENT_CACHE}, allEntries = true)
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

    private String generateDocumentId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "DOC-" + datePart + "-" + uniquePart;
    }

    /**
     * Build storage path based on category and channel
     * Format:
     *   - TEMPLATE: templates/{channel}/{documentId}/{filename}
     *   - GENERATED: generated/{channel}/{documentId}/{filename}
     *   - USER_UPLOAD: documents/{documentId}/{filename}
     */
    private String buildStoragePath(String documentId, String filename, DocumentCategory category, ChannelType channel) {
        if (category == DocumentCategory.TEMPLATE) {
            String channelPath = channel != null ? channel.name().toLowerCase() : "general";
            return String.format("templates/%s/%s/%s", channelPath, documentId, filename);
        } else if (category == DocumentCategory.GENERATED) {
            String channelPath = channel != null ? channel.name().toLowerCase() : "general";
            return String.format("generated/%s/%s/%s", channelPath, documentId, filename);
        }
        // Default for USER_UPLOAD
        return String.format("documents/%s/%s", documentId, filename);
    }

    /**
     * Parse document category from string, defaults to USER_UPLOAD
     */
    private DocumentCategory parseCategory(String category) {
        if (category == null || category.isEmpty()) {
            return DocumentCategory.USER_UPLOAD;
        }
        try {
            return DocumentCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document category: {}, defaulting to USER_UPLOAD", category);
            return DocumentCategory.USER_UPLOAD;
        }
    }

    /**
     * Parse channel type from string, can be null
     */
    private ChannelType parseChannel(String channel) {
        if (channel == null || channel.isEmpty()) {
            return null;
        }
        try {
            return ChannelType.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid channel type: {}", channel);
            return null;
        }
    }
}

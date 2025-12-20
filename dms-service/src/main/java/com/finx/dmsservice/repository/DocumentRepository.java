package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.domain.enums.ChannelType;
import com.finx.dmsservice.domain.enums.DocumentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Simple Document Repository
 * Provides basic CRUD operations for documents
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentId(String documentId);

    boolean existsByDocumentId(String documentId);

    // Filter by category
    Page<Document> findByDocumentCategory(DocumentCategory category, Pageable pageable);

    // Filter by channel
    Page<Document> findByChannel(ChannelType channel, Pageable pageable);

    // Filter by both category and channel
    Page<Document> findByDocumentCategoryAndChannel(DocumentCategory category, ChannelType channel, Pageable pageable);

    // Find by case ID (for generated documents)
    List<Document> findByCaseId(Long caseId);

    // Find by source template ID (for generated documents)
    List<Document> findBySourceTemplateId(Long sourceTemplateId);
}

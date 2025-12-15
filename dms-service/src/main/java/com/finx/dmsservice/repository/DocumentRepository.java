package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentId(String documentId);

    List<Document> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    Page<Document> findByDocumentType(DocumentType type, Pageable pageable);

    Page<Document> findByDocumentStatus(DocumentStatus status, Pageable pageable);

    Page<Document> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.entityType = :entityType AND d.entityId = :entityId AND d.documentStatus = 'ACTIVE'")
    List<Document> findActiveDocumentsByEntity(@Param("entityType") EntityType entityType, @Param("entityId") Long entityId);

    @Query("SELECT d FROM Document d WHERE d.documentStatus = 'ACTIVE' AND d.expiresAt IS NOT NULL AND d.expiresAt < :now")
    List<Document> findExpiredDocuments(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM Document d WHERE d.isArchived = true")
    Page<Document> findArchivedDocuments(Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.parentDocumentId = :parentId ORDER BY d.versionNumber DESC")
    List<Document> findVersionsByParentId(@Param("parentId") Long parentId);

    @Query("SELECT d FROM Document d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    Page<Document> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.documentStatus = :status")
    Long countByStatus(@Param("status") DocumentStatus status);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.entityType = :entityType AND d.entityId = :entityId")
    Long countByEntity(@Param("entityType") EntityType entityType, @Param("entityId") Long entityId);

    boolean existsByDocumentId(String documentId);

    List<Document> findByDocumentStatusOrderByCreatedAtDesc(DocumentStatus status);
}

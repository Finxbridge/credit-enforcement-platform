package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Simple Document Repository
 * Provides basic CRUD operations for documents
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentId(String documentId);

    boolean existsByDocumentId(String documentId);
}

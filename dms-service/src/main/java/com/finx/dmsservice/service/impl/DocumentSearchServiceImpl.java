package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.DocumentSearchRequest;
import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.mapper.DocumentMapper;
import com.finx.dmsservice.repository.DocumentRepository;
import com.finx.dmsservice.service.DocumentSearchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentSearchServiceImpl implements DocumentSearchService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper mapper;
    private final EntityManager entityManager;

    @Override
    public Page<DocumentDTO> search(DocumentSearchRequest request, Pageable pageable) {
        log.info("Searching documents with request: {}", request);
        return advancedSearch(request, pageable);
    }

    @Override
    public Page<DocumentDTO> searchByText(String searchText, Pageable pageable) {
        log.info("Searching documents by text: {}", searchText);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Document> query = cb.createQuery(Document.class);
        Root<Document> root = query.from(Document.class);

        String likePattern = "%" + searchText.toLowerCase() + "%";
        Predicate namePredicate = cb.like(cb.lower(root.get("documentName")), likePattern);
        Predicate descPredicate = cb.like(cb.lower(root.get("description")), likePattern);
        Predicate activePredicate = cb.equal(root.get("status"), com.finx.dmsservice.domain.enums.DocumentStatus.ACTIVE);

        query.where(cb.and(activePredicate, cb.or(namePredicate, descPredicate)));
        query.orderBy(cb.desc(root.get("uploadedAt")));

        TypedQuery<Document> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Document> documents = typedQuery.getResultList();
        long total = countByText(searchText);

        return new PageImpl<>(
                documents.stream().map(mapper::toDto).collect(Collectors.toList()),
                pageable,
                total
        );
    }

    @Override
    public List<DocumentDTO> searchByTags(List<String> tags) {
        log.info("Searching documents by tags: {}", tags);

        List<Document> allDocuments = documentRepository.findByDocumentStatusOrderByCreatedAtDesc(
                com.finx.dmsservice.domain.enums.DocumentStatus.ACTIVE);

        return allDocuments.stream()
                .filter(doc -> doc.getTags() != null && !doc.getTags().isEmpty())
                .filter(doc -> {
                    // Tags stored as JSON string or comma-separated, check if any tag exists
                    String docTags = doc.getTags().toLowerCase();
                    return tags.stream().anyMatch(tag -> docTags.contains(tag.toLowerCase()));
                })
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DocumentDTO> advancedSearch(DocumentSearchRequest request, Pageable pageable) {
        log.info("Advanced search with request: {}", request);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Document> query = cb.createQuery(Document.class);
        Root<Document> root = query.from(Document.class);

        List<Predicate> predicates = buildPredicates(cb, root, request);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("uploadedAt")));

        TypedQuery<Document> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Document> documents = typedQuery.getResultList();
        long total = countSearchResults(request);

        return new PageImpl<>(
                documents.stream().map(mapper::toDto).collect(Collectors.toList()),
                pageable,
                total
        );
    }

    @Override
    public long countSearchResults(DocumentSearchRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Document> root = query.from(Document.class);

        List<Predicate> predicates = buildPredicates(cb, root, request);
        query.select(cb.count(root));
        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Document> root, DocumentSearchRequest request) {
        List<Predicate> predicates = new ArrayList<>();

        // Default: only active documents
        predicates.add(cb.equal(root.get("status"), com.finx.dmsservice.domain.enums.DocumentStatus.ACTIVE));

        if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
            String likePattern = "%" + request.getSearchText().toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("documentName")), likePattern);
            Predicate descPredicate = cb.like(cb.lower(root.get("description")), likePattern);
            predicates.add(cb.or(namePredicate, descPredicate));
        }

        if (request.getDocumentTypes() != null && !request.getDocumentTypes().isEmpty()) {
            predicates.add(root.get("documentType").in(request.getDocumentTypes()));
        }

        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            predicates.remove(0); // Remove default active status
            predicates.add(root.get("status").in(request.getStatuses()));
        }

        if (request.getEntityTypes() != null && !request.getEntityTypes().isEmpty()) {
            predicates.add(root.get("entityType").in(request.getEntityTypes()));
        }

        if (request.getEntityId() != null) {
            predicates.add(cb.equal(root.get("entityId"), request.getEntityId()));
        }

        if (request.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("categoryId"), request.getCategoryId()));
        }

        if (request.getUploadedBy() != null) {
            predicates.add(cb.equal(root.get("uploadedBy"), request.getUploadedBy()));
        }

        if (request.getUploadedFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("uploadedAt"), request.getUploadedFrom()));
        }

        if (request.getUploadedTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("uploadedAt"), request.getUploadedTo()));
        }

        if (request.getMinSize() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fileSize"), request.getMinSize()));
        }

        if (request.getMaxSize() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fileSize"), request.getMaxSize()));
        }

        if (request.getFileExtension() != null && !request.getFileExtension().isEmpty()) {
            predicates.add(cb.equal(cb.lower(root.get("fileExtension")), request.getFileExtension().toLowerCase()));
        }

        return predicates;
    }

    private long countByText(String searchText) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Document> root = query.from(Document.class);

        String likePattern = "%" + searchText.toLowerCase() + "%";
        Predicate namePredicate = cb.like(cb.lower(root.get("documentName")), likePattern);
        Predicate descPredicate = cb.like(cb.lower(root.get("description")), likePattern);
        Predicate activePredicate = cb.equal(root.get("status"), com.finx.dmsservice.domain.enums.DocumentStatus.ACTIVE);

        query.select(cb.count(root));
        query.where(cb.and(activePredicate, cb.or(namePredicate, descPredicate)));

        return entityManager.createQuery(query).getSingleResult();
    }
}

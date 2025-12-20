package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.DocumentAccessLog;
import com.finx.dmsservice.domain.enums.AccessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, Long> {

    List<DocumentAccessLog> findByDocumentId(Long documentId);

    List<DocumentAccessLog> findByUserId(Long userId);

    Page<DocumentAccessLog> findByAccessType(AccessType accessType, Pageable pageable);

    @Query("SELECT a FROM DocumentAccessLog a WHERE a.documentId = :documentId ORDER BY a.accessedAt DESC")
    List<DocumentAccessLog> findRecentAccessByDocument(@Param("documentId") Long documentId);

    @Query("SELECT a FROM DocumentAccessLog a WHERE a.accessedAt BETWEEN :startDate AND :endDate")
    Page<DocumentAccessLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    @Query("SELECT COUNT(a) FROM DocumentAccessLog a WHERE a.documentId = :documentId")
    Long countAccessByDocument(@Param("documentId") Long documentId);
}

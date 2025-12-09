package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.CommunicationHistory;
import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
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
public interface CommunicationHistoryRepository extends JpaRepository<CommunicationHistory, Long> {

    Optional<CommunicationHistory> findByCommunicationId(String communicationId);

    List<CommunicationHistory> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    List<CommunicationHistory> findByCaseIdAndChannelOrderByCreatedAtDesc(Long caseId, CommunicationChannel channel);

    Page<CommunicationHistory> findByChannel(CommunicationChannel channel, Pageable pageable);

    Page<CommunicationHistory> findByStatus(CommunicationStatus status, Pageable pageable);

    Page<CommunicationHistory> findByExecutionId(Long executionId, Pageable pageable);

    List<CommunicationHistory> findByExecutionIdAndChannel(Long executionId, CommunicationChannel channel);

    Page<CommunicationHistory> findByStrategyId(Long strategyId, Pageable pageable);

    @Query("SELECT ch FROM CommunicationHistory ch WHERE ch.channel = :channel AND ch.createdAt BETWEEN :startDate AND :endDate ORDER BY ch.createdAt DESC")
    Page<CommunicationHistory> findByChannelAndDateRange(
            @Param("channel") CommunicationChannel channel,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT ch FROM CommunicationHistory ch WHERE ch.caseId = :caseId AND ch.channel = 'NOTICE' ORDER BY ch.createdAt DESC")
    List<CommunicationHistory> findNoticesByCaseId(@Param("caseId") Long caseId);

    @Query("SELECT ch FROM CommunicationHistory ch WHERE ch.hasDocument = true AND ch.channel = :channel ORDER BY ch.createdAt DESC")
    Page<CommunicationHistory> findWithDocumentsByChannel(@Param("channel") CommunicationChannel channel, Pageable pageable);

    @Query("SELECT COUNT(ch) FROM CommunicationHistory ch WHERE ch.executionId = :executionId AND ch.status = :status")
    Long countByExecutionIdAndStatus(@Param("executionId") Long executionId, @Param("status") CommunicationStatus status);

    @Query("SELECT ch.channel, COUNT(ch) FROM CommunicationHistory ch WHERE ch.caseId = :caseId GROUP BY ch.channel")
    List<Object[]> countByCaseIdGroupByChannel(@Param("caseId") Long caseId);
}

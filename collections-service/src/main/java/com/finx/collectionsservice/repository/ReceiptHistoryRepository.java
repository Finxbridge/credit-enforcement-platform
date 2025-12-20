package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ReceiptHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReceiptHistoryRepository extends JpaRepository<ReceiptHistory, Long> {

    List<ReceiptHistory> findByReceiptIdOrderByActionTimestampDesc(Long receiptId);

    List<ReceiptHistory> findByReceiptNumberOrderByActionTimestampDesc(String receiptNumber);

    Page<ReceiptHistory> findByActorId(Long actorId, Pageable pageable);

    @Query("SELECT rh FROM ReceiptHistory rh WHERE rh.action = :action " +
           "AND rh.actionTimestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY rh.actionTimestamp DESC")
    Page<ReceiptHistory> findByActionAndDateRange(
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COUNT(rh) FROM ReceiptHistory rh WHERE rh.action = :action " +
           "AND rh.actionTimestamp >= :since")
    Long countByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
}

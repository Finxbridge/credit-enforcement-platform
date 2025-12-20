package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    @Query("SELECT h FROM ApprovalHistory h WHERE h.approvalRequest.id = :requestId ORDER BY h.actionTimestamp ASC")
    List<ApprovalHistory> findByApprovalRequestIdOrderByActionTimestampAsc(@Param("requestId") Long requestId);

    @Query("SELECT h FROM ApprovalHistory h WHERE h.actorId = :actorId ORDER BY h.actionTimestamp DESC")
    List<ApprovalHistory> findByActorId(@Param("actorId") Long actorId);
}

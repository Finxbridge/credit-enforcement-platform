package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.DispatchStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchStatusHistoryRepository extends JpaRepository<DispatchStatusHistory, Long> {

    List<DispatchStatusHistory> findByDispatchTrackingIdOrderByEventTimestampDesc(Long dispatchId);

    List<DispatchStatusHistory> findByDispatchTrackingIdOrderByEventTimestampAsc(Long dispatchId);
}

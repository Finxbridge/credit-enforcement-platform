package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.LmsSyncHistory;
import com.finx.configurationsservice.domain.enums.SyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LmsSyncHistoryRepository extends JpaRepository<LmsSyncHistory, Long> {

    List<LmsSyncHistory> findByLmsConfigurationIdOrderByStartedAtDesc(Long lmsId);

    Page<LmsSyncHistory> findByLmsConfigurationId(Long lmsId, Pageable pageable);

    List<LmsSyncHistory> findTop10ByLmsConfigurationIdOrderByStartedAtDesc(Long lmsId);

    List<LmsSyncHistory> findBySyncStatus(SyncStatus syncStatus);

    List<LmsSyncHistory> findByLmsConfigurationIdAndSyncStatus(Long lmsId, SyncStatus syncStatus);
}

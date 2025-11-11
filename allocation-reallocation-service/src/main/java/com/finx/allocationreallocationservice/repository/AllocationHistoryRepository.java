package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.AllocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationHistoryRepository extends JpaRepository<AllocationHistory, Long> {

    List<AllocationHistory> findByCaseIdOrderByAllocatedAtDesc(Long caseId);

    List<AllocationHistory> findByAllocatedToUserIdOrderByAllocatedAtDesc(Long userId);
}

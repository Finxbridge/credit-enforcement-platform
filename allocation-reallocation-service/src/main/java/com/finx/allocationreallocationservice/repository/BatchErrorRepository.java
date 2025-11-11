package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.BatchError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchErrorRepository extends JpaRepository<BatchError, Long> {

    List<BatchError> findByBatchId(String batchId);

    Optional<BatchError> findByErrorId(String errorId);

    List<BatchError> findAllByOrderByCreatedAtDesc();

    List<BatchError> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}

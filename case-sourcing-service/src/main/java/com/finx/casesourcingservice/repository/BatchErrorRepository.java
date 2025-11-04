package com.finx.casesourcingservice.repository;

import com.finx.casesourcingservice.domain.entity.BatchError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchErrorRepository extends JpaRepository<BatchError, Long> {

    List<BatchError> findByBatchId(String batchId);

    Long countByBatchId(String batchId);
}

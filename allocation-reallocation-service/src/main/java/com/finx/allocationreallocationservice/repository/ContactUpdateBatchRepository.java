package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.ContactUpdateBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactUpdateBatchRepository extends JpaRepository<ContactUpdateBatch, Long> {

    Optional<ContactUpdateBatch> findByBatchId(String batchId);
    boolean existsByBatchId(String batchId);
}

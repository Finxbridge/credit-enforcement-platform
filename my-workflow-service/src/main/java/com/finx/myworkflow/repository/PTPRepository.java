package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.PTPCommitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PTP commitment queries (read-only for workflow)
 */
@Repository
public interface PTPRepository extends JpaRepository<PTPCommitment, Long> {

    List<PTPCommitment> findByCaseIdOrderByPtpDateDesc(Long caseId);
}

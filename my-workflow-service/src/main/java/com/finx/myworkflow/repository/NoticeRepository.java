package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for notice queries (read-only for workflow)
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}

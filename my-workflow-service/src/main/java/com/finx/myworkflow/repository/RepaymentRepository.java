package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for repayment queries (read-only for workflow)
 */
@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findByCaseIdOrderByPaymentDateDesc(Long caseId);
}

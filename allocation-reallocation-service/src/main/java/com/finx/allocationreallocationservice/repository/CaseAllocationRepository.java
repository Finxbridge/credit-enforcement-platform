package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.CaseAllocation;
import com.finx.allocationreallocationservice.domain.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseAllocationRepository extends JpaRepository<CaseAllocation, Long>, JpaSpecificationExecutor<CaseAllocation> {

    Optional<CaseAllocation> findFirstByCaseIdOrderByAllocatedAtDesc(Long caseId);

    List<CaseAllocation> findByPrimaryAgentId(Long primaryAgentId);

    long countByPrimaryAgentIdAndStatus(Long primaryAgentId, AllocationStatus status);

}

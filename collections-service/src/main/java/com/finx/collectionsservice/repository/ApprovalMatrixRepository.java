package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ApprovalMatrix;
import com.finx.collectionsservice.domain.enums.ApprovalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalMatrixRepository extends JpaRepository<ApprovalMatrix, Long> {

    Optional<ApprovalMatrix> findByMatrixCode(String matrixCode);

    boolean existsByMatrixCode(String matrixCode);

    List<ApprovalMatrix> findByIsActiveTrueOrderByPriorityOrderDesc();

    List<ApprovalMatrix> findByApprovalTypeAndIsActiveTrueOrderByPriorityOrderDesc(ApprovalType approvalType);

    @Query("SELECT m FROM ApprovalMatrix m WHERE m.approvalType = :type AND m.isActive = true " +
           "AND (m.minAmount IS NULL OR m.minAmount <= :amount) " +
           "AND (m.maxAmount IS NULL OR m.maxAmount >= :amount) " +
           "ORDER BY m.priorityOrder DESC")
    List<ApprovalMatrix> findMatchingMatrixByAmount(
            @Param("type") ApprovalType type,
            @Param("amount") BigDecimal amount);

    @Query("SELECT m FROM ApprovalMatrix m WHERE m.approvalType = :type AND m.isActive = true " +
           "AND m.approvalLevel = :level ORDER BY m.priorityOrder DESC")
    List<ApprovalMatrix> findByApprovalTypeAndLevel(
            @Param("type") ApprovalType type,
            @Param("level") Integer level);

    @Query("SELECT m FROM ApprovalMatrix m WHERE " +
           "(:type IS NULL OR m.approvalType = :type) AND " +
           "(:isActive IS NULL OR m.isActive = :isActive)")
    Page<ApprovalMatrix> findWithFilters(
            @Param("type") ApprovalType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}

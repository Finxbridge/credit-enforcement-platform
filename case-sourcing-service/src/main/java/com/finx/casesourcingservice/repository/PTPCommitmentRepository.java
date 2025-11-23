package com.finx.casesourcingservice.repository;

import com.finx.casesourcingservice.domain.entity.PTPCommitment;
import com.finx.casesourcingservice.domain.enums.PTPStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PTP Commitment operations
 */
@Repository
public interface PTPCommitmentRepository extends JpaRepository<PTPCommitment, Long> {

    /**
     * Find all PTPs for a specific case
     */
    List<PTPCommitment> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    /**
     * Find latest PTP for a case
     */
    Optional<PTPCommitment> findFirstByCaseIdOrderByCreatedAtDesc(Long caseId);

    /**
     * Find PTPs by status
     */
    Page<PTPCommitment> findByPtpStatus(PTPStatus status, Pageable pageable);

    /**
     * Find PTPs due on a specific date with PENDING status
     * FR-PTP-2: Get PTPs due today
     */
    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate = :dueDate AND p.ptpStatus = 'PENDING'")
    List<PTPCommitment> findPTPsDueOnDate(@Param("dueDate") LocalDate dueDate);

    /**
     * Find PTPs due today for a specific user
     */
    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate = :dueDate AND p.ptpStatus = 'PENDING' AND p.userId = :userId")
    List<PTPCommitment> findPTPsDueForUser(@Param("dueDate") LocalDate dueDate, @Param("userId") Long userId);

    /**
     * Find broken PTPs (past due date with PENDING status)
     * FR-PTP-3: Get broken PTPs for follow-up
     */
    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate < :today AND p.ptpStatus = 'PENDING'")
    List<PTPCommitment> findBrokenPTPs(@Param("today") LocalDate today);

    /**
     * Find broken PTPs for a specific user
     */
    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate < :today AND p.ptpStatus = 'PENDING' AND p.userId = :userId")
    List<PTPCommitment> findBrokenPTPsForUser(@Param("today") LocalDate today, @Param("userId") Long userId);

    /**
     * Find PTPs requiring reminder (1 day before due date, reminder not sent)
     */
    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate = :tomorrowDate AND p.ptpStatus = 'PENDING' AND p.reminderSent = false")
    List<PTPCommitment> findPTPsRequiringReminder(@Param("tomorrowDate") LocalDate tomorrowDate);

    /**
     * Count pending PTPs for a user
     */
    @Query("SELECT COUNT(p) FROM PTPCommitment p WHERE p.userId = :userId AND p.ptpStatus = 'PENDING'")
    Long countPendingPTPsForUser(@Param("userId") Long userId);

    /**
     * Count broken PTPs for a user
     */
    @Query("SELECT COUNT(p) FROM PTPCommitment p WHERE p.userId = :userId AND p.ptpStatus = 'BROKEN'")
    Long countBrokenPTPsForUser(@Param("userId") Long userId);

    /**
     * Get PTP statistics for a date range
     */
    @Query("SELECT p.ptpStatus, COUNT(p) FROM PTPCommitment p WHERE p.ptpDate BETWEEN :startDate AND :endDate GROUP BY p.ptpStatus")
    List<Object[]> getPTPStatsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find all PTPs for multiple cases
     */
    @Query("SELECT p FROM PTPCommitment p WHERE p.caseId IN :caseIds ORDER BY p.createdAt DESC")
    List<PTPCommitment> findByCaseIdIn(@Param("caseIds") List<Long> caseIds);
}

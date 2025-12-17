package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.PTPCommitment;
import com.finx.collectionsservice.domain.enums.PTPStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PTPCommitmentRepository extends JpaRepository<PTPCommitment, Long> {

    List<PTPCommitment> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    Optional<PTPCommitment> findFirstByCaseIdOrderByCreatedAtDesc(Long caseId);

    Page<PTPCommitment> findByPtpStatus(PTPStatus status, Pageable pageable);

    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate = :dueDate AND p.ptpStatus = 'PENDING'")
    List<PTPCommitment> findPTPsDueOnDate(@Param("dueDate") LocalDate dueDate);

    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate = :dueDate AND p.ptpStatus = 'PENDING' AND p.userId = :userId")
    List<PTPCommitment> findPTPsDueForUser(@Param("dueDate") LocalDate dueDate, @Param("userId") Long userId);

    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate < :today AND p.ptpStatus = 'PENDING'")
    List<PTPCommitment> findBrokenPTPs(@Param("today") LocalDate today);

    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate < :today AND p.ptpStatus = 'PENDING' AND p.userId = :userId")
    List<PTPCommitment> findBrokenPTPsForUser(@Param("today") LocalDate today, @Param("userId") Long userId);

    @Query("SELECT p FROM PTPCommitment p WHERE p.ptpDate = :tomorrowDate AND p.ptpStatus = 'PENDING' AND p.reminderSent = false")
    List<PTPCommitment> findPTPsRequiringReminder(@Param("tomorrowDate") LocalDate tomorrowDate);

    @Query("SELECT COUNT(p) FROM PTPCommitment p WHERE p.userId = :userId AND p.ptpStatus = 'PENDING'")
    Long countPendingPTPsForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM PTPCommitment p WHERE p.userId = :userId AND p.ptpStatus = 'BROKEN'")
    Long countBrokenPTPsForUser(@Param("userId") Long userId);

    @Query("SELECT p.ptpStatus, COUNT(p) FROM PTPCommitment p WHERE p.ptpDate BETWEEN :startDate AND :endDate GROUP BY p.ptpStatus")
    List<Object[]> getPTPStatsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM PTPCommitment p WHERE p.caseId IN :caseIds ORDER BY p.createdAt DESC")
    List<PTPCommitment> findByCaseIdIn(@Param("caseIds") List<Long> caseIds);

    List<PTPCommitment> findByUserId(Long userId);
}

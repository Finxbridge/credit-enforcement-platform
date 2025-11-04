package com.finx.casesourcingservice.repository;

import com.finx.casesourcingservice.domain.entity.CaseBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseBatchRepository extends JpaRepository<CaseBatch, Long> {

       Optional<CaseBatch> findByBatchId(String batchId);

       boolean existsByBatchId(String batchId);

       Page<CaseBatch> findAllByOrderByCreatedAtDesc(Pageable pageable);

       @Query("SELECT cb.sourceType as source, SUM(cb.totalCases) as total, " +
                     "SUM(cb.validCases) as successful, SUM(cb.invalidCases) as failed " +
                     "FROM CaseBatch cb GROUP BY cb.sourceType")
       List<Object[]> getSourceStats();

       @Query("SELECT SUM(cb.totalCases) FROM CaseBatch cb")
       Long getTotalReceived();

       @Query("SELECT SUM(cb.validCases) FROM CaseBatch cb")
       Long getTotalValidated();

       @Query("SELECT SUM(cb.invalidCases) FROM CaseBatch cb")
       Long getTotalFailed();

       // Intake Report Queries
       @Query("SELECT cb FROM CaseBatch cb WHERE cb.createdAt BETWEEN :startDate AND :endDate ORDER BY cb.createdAt")
       List<CaseBatch> findBatchesByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       @Query("SELECT CAST(cb.createdAt AS date) as date, SUM(cb.totalCases) as total, " +
                     "SUM(cb.validCases) as validated, SUM(cb.invalidCases) as failed " +
                     "FROM CaseBatch cb WHERE cb.createdAt BETWEEN :startDate AND :endDate " +
                     "GROUP BY CAST(cb.createdAt AS date) ORDER BY date")
       List<Object[]> getDailyIntakeStats(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       @Query("SELECT cb.sourceType as source, SUM(cb.totalCases) as total, " +
                     "SUM(cb.validCases) as validated, SUM(cb.invalidCases) as failed " +
                     "FROM CaseBatch cb WHERE cb.createdAt BETWEEN :startDate AND :endDate " +
                     "GROUP BY cb.sourceType")
       List<Object[]> getSourceWiseIntakeStats(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);
}

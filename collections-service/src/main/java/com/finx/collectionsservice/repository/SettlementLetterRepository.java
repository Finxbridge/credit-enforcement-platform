package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.SettlementLetter;
import com.finx.collectionsservice.domain.enums.LetterStatus;
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
public interface SettlementLetterRepository extends JpaRepository<SettlementLetter, Long> {

    Optional<SettlementLetter> findByLetterNumber(String letterNumber);

    Optional<SettlementLetter> findByOtsId(Long otsId);

    List<SettlementLetter> findByCaseId(Long caseId);

    Page<SettlementLetter> findByStatus(LetterStatus status, Pageable pageable);

    @Query("SELECT l FROM SettlementLetter l WHERE l.expiresAt <= :dateTime AND l.status != 'EXPIRED'")
    List<SettlementLetter> findExpiredLetters(@Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT COUNT(l) FROM SettlementLetter l WHERE l.status = :status")
    Long countByStatus(@Param("status") LetterStatus status);

    boolean existsByLetterNumber(String letterNumber);

    boolean existsByOtsId(Long otsId);
}

package com.finx.communication.repository;

import com.finx.communication.domain.entity.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {

    Optional<SmsMessage> findByMessageId(String messageId);

    Optional<SmsMessage> findByProviderMessageId(String providerMessageId);

    List<SmsMessage> findByCampaignId(Long campaignId);

    List<SmsMessage> findByCaseId(Long caseId);

    @Query("SELECT s FROM SmsMessage s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<SmsMessage> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<SmsMessage> findByStatus(String status);
}

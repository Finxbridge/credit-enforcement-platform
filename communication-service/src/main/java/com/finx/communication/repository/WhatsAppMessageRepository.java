package com.finx.communication.repository;

import com.finx.communication.domain.entity.WhatsAppMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, Long> {

    Optional<WhatsAppMessage> findByMessageId(String messageId);

    Optional<WhatsAppMessage> findByProviderMessageId(String providerMessageId);

    List<WhatsAppMessage> findByCampaignId(Long campaignId);

    List<WhatsAppMessage> findByCaseId(Long caseId);

    @Query("SELECT w FROM WhatsAppMessage w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<WhatsAppMessage> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<WhatsAppMessage> findByStatus(String status);
}

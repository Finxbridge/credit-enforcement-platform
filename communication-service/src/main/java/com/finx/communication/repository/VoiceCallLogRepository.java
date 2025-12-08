package com.finx.communication.repository;

import com.finx.communication.domain.entity.VoiceCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoiceCallLogRepository extends JpaRepository<VoiceCallLog, Long> {

    Optional<VoiceCallLog> findByCallId(String callId);
}

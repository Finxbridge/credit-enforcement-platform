package com.finx.communication.repository;

import com.finx.communication.domain.entity.DialerCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialerCallLogRepository extends JpaRepository<DialerCallLog, Long> {

    Optional<DialerCallLog> findByCallId(String callId);

    Optional<DialerCallLog> findByDialerCallId(String dialerCallId);

    List<DialerCallLog> findByAgentId(Long agentId);

    List<DialerCallLog> findByCaseId(Long caseId);

    List<DialerCallLog> findByCallStatus(String callStatus);
}

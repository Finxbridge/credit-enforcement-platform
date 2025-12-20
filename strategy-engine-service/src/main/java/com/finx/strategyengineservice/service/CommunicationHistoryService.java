package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.dto.CommunicationHistoryDTO;
import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing communication history
 */
public interface CommunicationHistoryService {

    CommunicationHistoryDTO getById(Long id);

    CommunicationHistoryDTO getByCommunicationId(String communicationId);

    List<CommunicationHistoryDTO> getByCaseId(Long caseId);

    List<CommunicationHistoryDTO> getByCaseIdAndChannel(Long caseId, CommunicationChannel channel);

    Page<CommunicationHistoryDTO> getByChannel(CommunicationChannel channel, Pageable pageable);

    Page<CommunicationHistoryDTO> getByStatus(CommunicationStatus status, Pageable pageable);

    Page<CommunicationHistoryDTO> getByExecutionId(Long executionId, Pageable pageable);

    Page<CommunicationHistoryDTO> getByChannelAndDateRange(
            CommunicationChannel channel,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get all notices sent for a case - useful for "Notices Sent" tab
     */
    List<CommunicationHistoryDTO> getNoticesByCaseId(Long caseId);

    /**
     * Get all communications with documents
     */
    Page<CommunicationHistoryDTO> getWithDocumentsByChannel(CommunicationChannel channel, Pageable pageable);

    /**
     * Get communication stats by case
     */
    List<Object[]> getStatsByCaseId(Long caseId);
}

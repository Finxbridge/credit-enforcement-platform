package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.CommunicationHistoryDTO;
import com.finx.strategyengineservice.domain.entity.CommunicationHistory;
import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
import com.finx.strategyengineservice.exception.BusinessException;
import com.finx.strategyengineservice.repository.CommunicationHistoryRepository;
import com.finx.strategyengineservice.service.CommunicationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommunicationHistoryServiceImpl implements CommunicationHistoryService {

    private final CommunicationHistoryRepository repository;

    @Override
    public CommunicationHistoryDTO getById(Long id) {
        CommunicationHistory entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Communication history not found with ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    public CommunicationHistoryDTO getByCommunicationId(String communicationId) {
        CommunicationHistory entity = repository.findByCommunicationId(communicationId)
                .orElseThrow(() -> new BusinessException("Communication history not found with ID: " + communicationId));
        return mapToDTO(entity);
    }

    @Override
    public List<CommunicationHistoryDTO> getByCaseId(Long caseId) {
        return repository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunicationHistoryDTO> getByCaseIdAndChannel(Long caseId, CommunicationChannel channel) {
        return repository.findByCaseIdAndChannelOrderByCreatedAtDesc(caseId, channel).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommunicationHistoryDTO> getByChannel(CommunicationChannel channel, Pageable pageable) {
        return repository.findByChannel(channel, pageable).map(this::mapToDTO);
    }

    @Override
    public Page<CommunicationHistoryDTO> getByStatus(CommunicationStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(this::mapToDTO);
    }

    @Override
    public Page<CommunicationHistoryDTO> getByExecutionId(Long executionId, Pageable pageable) {
        return repository.findByExecutionId(executionId, pageable).map(this::mapToDTO);
    }

    @Override
    public Page<CommunicationHistoryDTO> getByChannelAndDateRange(
            CommunicationChannel channel,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return repository.findByChannelAndDateRange(channel, startDate, endDate, pageable).map(this::mapToDTO);
    }

    @Override
    public List<CommunicationHistoryDTO> getNoticesByCaseId(Long caseId) {
        return repository.findNoticesByCaseId(caseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommunicationHistoryDTO> getWithDocumentsByChannel(CommunicationChannel channel, Pageable pageable) {
        return repository.findWithDocumentsByChannel(channel, pageable).map(this::mapToDTO);
    }

    @Override
    public List<Object[]> getStatsByCaseId(Long caseId) {
        return repository.countByCaseIdGroupByChannel(caseId);
    }

    private CommunicationHistoryDTO mapToDTO(CommunicationHistory entity) {
        return CommunicationHistoryDTO.builder()
                .id(entity.getId())
                .communicationId(entity.getCommunicationId())
                .caseId(entity.getCaseId())
                .executionId(entity.getExecutionId())
                .strategyId(entity.getStrategyId())
                .actionId(entity.getActionId())
                .channel(entity.getChannel())
                .templateId(entity.getTemplateId())
                .templateCode(entity.getTemplateCode())
                .recipientMobile(entity.getRecipientMobile())
                .recipientEmail(entity.getRecipientEmail())
                .recipientName(entity.getRecipientName())
                .recipientAddress(entity.getRecipientAddress())
                .subject(entity.getSubject())
                .content(entity.getContent())
                .hasDocument(entity.getHasDocument())
                .dmsDocumentId(entity.getDmsDocumentId())
                .originalDocumentUrl(entity.getOriginalDocumentUrl())
                .processedDocumentUrl(entity.getProcessedDocumentUrl())
                .documentType(entity.getDocumentType())
                .documentOriginalName(entity.getDocumentOriginalName())
                .status(entity.getStatus())
                .providerMessageId(entity.getProviderMessageId())
                .providerResponse(entity.getProviderResponse())
                .failureReason(entity.getFailureReason())
                .noticeId(entity.getNoticeId())
                .noticeNumber(entity.getNoticeNumber())
                .sentAt(entity.getSentAt())
                .deliveredAt(entity.getDeliveredAt())
                .failedAt(entity.getFailedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

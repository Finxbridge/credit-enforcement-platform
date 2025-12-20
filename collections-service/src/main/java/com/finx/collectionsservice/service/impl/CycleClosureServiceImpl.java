package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.CaseClosureResponse;
import com.finx.collectionsservice.domain.entity.CycleClosure;
import com.finx.collectionsservice.domain.enums.ClosureStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.repository.CycleClosureRepository;
import com.finx.collectionsservice.service.CycleClosureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CycleClosureServiceImpl implements CycleClosureService {

    private final CycleClosureRepository closureRepository;

    @Override
    @Transactional
    public CaseClosureResponse closeCase(Long caseId, String closureReason) {
        log.info("Closing case {} with reason: {}", caseId, closureReason);

        // Check if case is already closed
        closureRepository.findByCaseId(caseId).ifPresent(existing -> {
            if (existing.getClosureStatus() == ClosureStatus.COMPLETED) {
                throw new BusinessException("Case is already closed");
            }
        });

        String executionId = UUID.randomUUID().toString();

        CycleClosure closure = CycleClosure.builder()
                .executionId(executionId)
                .caseId(caseId)
                .closureStatus(ClosureStatus.COMPLETED)
                .closureReason(closureReason)
                .archivedAt(LocalDateTime.now())
                .build();

        closureRepository.save(closure);

        log.info("Case {} closed successfully", caseId);

        return CaseClosureResponse.builder()
                .totalRequested(1)
                .totalClosed(1)
                .totalFailed(0)
                .closureReason(closureReason)
                .closedAt(LocalDateTime.now())
                .closedCaseIds(List.of(caseId))
                .failedCaseIds(new ArrayList<>())
                .message("Case closed successfully")
                .build();
    }

    @Override
    @Transactional
    public CaseClosureResponse closeCasesBulk(List<Long> caseIds, String closureReason) {
        log.info("Closing {} cases in bulk", caseIds.size());

        String executionId = UUID.randomUUID().toString();
        List<Long> closedCaseIds = new ArrayList<>();
        List<Long> failedCaseIds = new ArrayList<>();

        for (Long caseId : caseIds) {
            try {
                // Check if already closed
                if (closureRepository.findByCaseId(caseId)
                        .filter(c -> c.getClosureStatus() == ClosureStatus.COMPLETED)
                        .isPresent()) {
                    failedCaseIds.add(caseId);
                    continue;
                }

                CycleClosure closure = CycleClosure.builder()
                        .executionId(executionId)
                        .caseId(caseId)
                        .closureStatus(ClosureStatus.COMPLETED)
                        .closureReason(closureReason)
                        .archivedAt(LocalDateTime.now())
                        .build();

                closureRepository.save(closure);
                closedCaseIds.add(caseId);
            } catch (Exception e) {
                log.error("Failed to close case {}: {}", caseId, e.getMessage());
                failedCaseIds.add(caseId);
            }
        }

        log.info("Bulk closure completed: {} closed, {} failed", closedCaseIds.size(), failedCaseIds.size());

        return CaseClosureResponse.builder()
                .totalRequested(caseIds.size())
                .totalClosed(closedCaseIds.size())
                .totalFailed(failedCaseIds.size())
                .closureReason(closureReason)
                .closedAt(LocalDateTime.now())
                .closedCaseIds(closedCaseIds)
                .failedCaseIds(failedCaseIds)
                .message(String.format("Closed %d of %d cases", closedCaseIds.size(), caseIds.size()))
                .build();
    }

    @Override
    @Transactional
    public CaseClosureResponse reopenCase(Long caseId) {
        log.info("Reopening case {}", caseId);

        CycleClosure closure = closureRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case closure", caseId));

        if (closure.getClosureStatus() != ClosureStatus.COMPLETED) {
            throw new BusinessException("Case is not in closed status");
        }

        closureRepository.delete(closure);

        log.info("Case {} reopened successfully", caseId);

        return CaseClosureResponse.builder()
                .totalRequested(1)
                .totalClosed(0)
                .totalFailed(0)
                .closedAt(null)
                .closedCaseIds(new ArrayList<>())
                .failedCaseIds(new ArrayList<>())
                .message("Case reopened successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseClosureResponse> getClosedCases(Pageable pageable) {
        log.info("Fetching closed cases");

        Page<CycleClosure> closures = closureRepository.findByClosureStatus(ClosureStatus.COMPLETED, pageable);

        return closures.map(c -> CaseClosureResponse.builder()
                .totalRequested(1)
                .totalClosed(1)
                .totalFailed(0)
                .closureReason(c.getClosureReason())
                .closedAt(c.getArchivedAt())
                .closedCaseIds(List.of(c.getCaseId()))
                .failedCaseIds(new ArrayList<>())
                .message("Case closed")
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaseClosureResponse> getCaseClosureHistory(Long caseId) {
        log.info("Fetching closure history for case {}", caseId);

        return closureRepository.findByCaseId(caseId)
                .map(c -> List.of(CaseClosureResponse.builder()
                        .totalRequested(1)
                        .totalClosed(1)
                        .totalFailed(0)
                        .closureReason(c.getClosureReason())
                        .closedAt(c.getArchivedAt())
                        .closedCaseIds(List.of(c.getCaseId()))
                        .failedCaseIds(new ArrayList<>())
                        .message("Case closure record")
                        .build()))
                .orElse(new ArrayList<>());
    }
}

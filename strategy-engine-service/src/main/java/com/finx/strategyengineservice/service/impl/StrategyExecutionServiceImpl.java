package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.ExecutionDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionDetailDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionInitiatedDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionRunDetailsDTO;
import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.entity.StrategyExecution;
import com.finx.strategyengineservice.domain.enums.ExecutionStatus;
import com.finx.strategyengineservice.domain.enums.ExecutionType;
import com.finx.strategyengineservice.exception.BusinessException;
import com.finx.strategyengineservice.repository.StrategyExecutionRepository;
import com.finx.strategyengineservice.repository.StrategyRepository;
import com.finx.strategyengineservice.service.StrategyExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StrategyExecutionServiceImpl implements StrategyExecutionService {

    private final StrategyExecutionRepository executionRepository;
    private final StrategyRepository strategyRepository;

    @Override
    @CacheEvict(value = {"strategyExecutions", "executionDetails"}, allEntries = true)
    public ExecutionInitiatedDTO executeStrategy(Long strategyId) {
        log.info("Initiating execution for strategy ID: {}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        // Create execution record
        StrategyExecution execution = StrategyExecution.builder()
                .executionId("exec_" + System.currentTimeMillis())
                .strategyId(strategy.getId())
                .strategyName(strategy.getStrategyName())
                .executionType(ExecutionType.MANUAL)
                .executionStatus(ExecutionStatus.PROCESSING)
                .startedAt(LocalDateTime.now())
                .build();

        StrategyExecution savedExecution = executionRepository.save(execution);
        log.info("Execution initiated with ID: {}", savedExecution.getExecutionId());

        // Trigger async processing
        processStrategyAsync(savedExecution.getId(), strategy.getId());

        return ExecutionInitiatedDTO.builder()
                .executionId(savedExecution.getExecutionId())
                .strategyId(strategyId)
                .status(ExecutionStatus.PROCESSING.name())
                .build();
    }

    @Async("strategyExecutionExecutor")
    protected void processStrategyAsync(Long executionId, Long strategyId) {
        try {
            log.info("Processing strategy execution async: {}", executionId);

            // Simulate processing
            Thread.sleep(2000);

            // Update execution with results
            StrategyExecution execution = executionRepository.findById(executionId)
                    .orElseThrow(() -> new BusinessException("Execution not found"));

            // Simulate results
            Random random = new Random();
            int totalProcessed = random.nextInt(500) + 100;
            int successful = (int) (totalProcessed * 0.95);
            int failed = totalProcessed - successful;

            execution.setExecutionStatus(ExecutionStatus.COMPLETED);
            execution.setTotalCasesProcessed(totalProcessed);
            execution.setSuccessfulActions(successful);
            execution.setFailedActions(failed);
            execution.setCompletedAt(LocalDateTime.now());

            // Create sample execution log
            if (failed > 0) {
                List<Map<String, Object>> errors = new ArrayList<>();
                for (int i = 0; i < Math.min(failed, 10); i++) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("caseId", 1000 + i);
                    error.put("action", "SEND_SMS");
                    error.put("error", "Invalid mobile number");
                    errors.add(error);
                }
                execution.setExecutionLog(errors);
            }

            executionRepository.save(execution);

            // Update strategy statistics
            Strategy strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new BusinessException("Strategy not found"));
            strategy.setLastRunAt(LocalDateTime.now());
            strategy.setSuccessCount(strategy.getSuccessCount() + successful);
            strategy.setFailureCount(strategy.getFailureCount() + failed);
            strategyRepository.save(strategy);

            log.info("Strategy execution completed successfully: {}", execution.getExecutionId());

        } catch (Exception e) {
            log.error("Error processing strategy execution: {}", executionId, e);
            StrategyExecution execution = executionRepository.findById(executionId).orElse(null);
            if (execution != null) {
                execution.setExecutionStatus(ExecutionStatus.FAILED);
                execution.setCompletedAt(LocalDateTime.now());
                executionRepository.save(execution);
            }
        }
    }

    @Override
    @Cacheable(value = "strategyExecutions", key = "'all'")
    @Transactional(readOnly = true)
    public List<ExecutionDTO> getAllExecutions() {
        log.info("Fetching all strategy executions");
        List<StrategyExecution> executions = executionRepository.findAllByOrderByStartedAtDesc(null).getContent();

        return executions.stream()
                .map(this::convertToExecutionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "executionDetails", key = "#executionId")
    @Transactional(readOnly = true)
    public ExecutionDetailDTO getExecutionDetails(String executionId) {
        log.info("Fetching execution details for ID: {}", executionId);
        StrategyExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new BusinessException("Execution not found with ID: " + executionId));

        return convertToExecutionDetailDTO(execution);
    }

    @Override
    @Cacheable(value = "executionDetails", key = "'details_' + #executionId")
    @Transactional(readOnly = true)
    public ExecutionRunDetailsDTO getExecutionRunDetails(String executionId) {
        log.info("Fetching execution run details for ID: {}", executionId);
        StrategyExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new BusinessException("Execution not found with ID: " + executionId));

        return convertToExecutionRunDetailsDTO(execution);
    }

    // Conversion methods
    private ExecutionDTO convertToExecutionDTO(StrategyExecution execution) {
        return ExecutionDTO.builder()
                .executionId(execution.getExecutionId())
                .strategyId(execution.getStrategyId())
                .strategyName(execution.getStrategyName())
                .status(execution.getExecutionStatus().name())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .totalCasesProcessed(execution.getTotalCasesProcessed())
                .successfulActions(execution.getSuccessfulActions())
                .failedActions(execution.getFailedActions())
                .build();
    }

    private ExecutionDetailDTO convertToExecutionDetailDTO(StrategyExecution execution) {
        return ExecutionDetailDTO.builder()
                .executionId(execution.getExecutionId())
                .strategyId(execution.getStrategyId())
                .strategyName(execution.getStrategyName())
                .status(execution.getExecutionStatus().name())
                .totalCasesProcessed(execution.getTotalCasesProcessed())
                .successfulActions(execution.getSuccessfulActions())
                .failedActions(execution.getFailedActions())
                .errors(execution.getExecutionLog())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .build();
    }

    private ExecutionRunDetailsDTO convertToExecutionRunDetailsDTO(StrategyExecution execution) {
        // Convert execution log to details format as per API spec
        List<Map<String, Object>> details = new ArrayList<>();

        if (execution.getExecutionLog() != null) {
            details.addAll(execution.getExecutionLog());
        }

        return ExecutionRunDetailsDTO.builder()
                .executionId(execution.getExecutionId())
                .details(details)
                .build();
    }
}

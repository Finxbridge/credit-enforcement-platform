package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.DashboardDTO;
import com.finx.collectionsservice.domain.dto.PTPStatsDTO;
import com.finx.collectionsservice.domain.entity.ArchivalRule;
import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import com.finx.collectionsservice.repository.*;
import com.finx.collectionsservice.service.DashboardService;
import com.finx.collectionsservice.service.PTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ArchivalRuleRepository archivalRuleRepository;
    private final RepaymentRepository repaymentRepository;
    private final OTSRequestRepository otsRepository;
    private final SettlementLetterRepository letterRepository;
    private final PTPService ptpService;

    @Override
    @Cacheable(value = "cycleClosureDashboard", key = "'dashboard'")
    public DashboardDTO.CycleClosureDashboard getCycleClosureDashboard() {
        log.info("Building cycle closure dashboard");

        List<ArchivalRule> activeRules = archivalRuleRepository.findActiveRules();
        long totalArchived = activeRules.stream()
                .mapToLong(r -> r.getTotalCasesArchived() != null ? r.getTotalCasesArchived() : 0L)
                .sum();

        List<DashboardDTO.RecentExecution> recentExecutions = activeRules.stream()
                .filter(r -> r.getLastExecutionAt() != null)
                .sorted((a, b) -> b.getLastExecutionAt().compareTo(a.getLastExecutionAt()))
                .limit(5)
                .map(r -> DashboardDTO.RecentExecution.builder()
                        .executionId(r.getRuleCode())
                        .ruleName(r.getRuleName())
                        .casesArchived(r.getLastCasesArchived() != null ? r.getLastCasesArchived() : 0)
                        .status(r.getLastExecutionResult())
                        .executedAt(r.getLastExecutionAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardDTO.CycleClosureDashboard.builder()
                .activeRulesCount((long) activeRules.size())
                .totalArchivedAllTime(totalArchived)
                .recentExecutions(recentExecutions)
                .build();
    }

    @Override
    @Cacheable(value = "repaymentDashboard", key = "'dashboard'")
    public DashboardDTO.RepaymentDashboard getRepaymentDashboard() {
        log.info("Building repayment dashboard");

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal totalToday = repaymentRepository.sumPaymentAmountByDateRange(startOfDay, now);
        BigDecimal totalThisMonth = repaymentRepository.sumPaymentAmountByDateRange(startOfMonth, now);
        Long transactionsToday = repaymentRepository.countByDateRange(startOfDay, now);
        Long transactionsThisMonth = repaymentRepository.countByDateRange(startOfMonth, now);
        Long pendingApprovals = repaymentRepository.countByApprovalStatus(RepaymentStatus.PENDING);

        List<Object[]> paymentModeStats = repaymentRepository.sumByPaymentMode(startOfMonth, now);
        Map<String, BigDecimal> collectionByPaymentMode = new HashMap<>();
        for (Object[] stat : paymentModeStats) {
            String mode = stat[0] != null ? stat[0].toString() : "UNKNOWN";
            BigDecimal amount = stat[1] != null ? (BigDecimal) stat[1] : BigDecimal.ZERO;
            collectionByPaymentMode.put(mode, amount);
        }

        Map<String, Long> transactionsByStatus = new HashMap<>();
        transactionsByStatus.put("PENDING", repaymentRepository.countByApprovalStatus(RepaymentStatus.PENDING));
        transactionsByStatus.put("APPROVED", repaymentRepository.countByApprovalStatus(RepaymentStatus.APPROVED));
        transactionsByStatus.put("REJECTED", repaymentRepository.countByApprovalStatus(RepaymentStatus.REJECTED));

        BigDecimal avgAmount = transactionsThisMonth > 0 && totalThisMonth != null ?
                totalThisMonth.divide(BigDecimal.valueOf(transactionsThisMonth), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Long approved = transactionsByStatus.getOrDefault("APPROVED", 0L);
        Long total = transactionsByStatus.values().stream().mapToLong(Long::longValue).sum();
        Double approvalRate = total > 0 ? (approved * 100.0) / total : 0.0;

        return DashboardDTO.RepaymentDashboard.builder()
                .totalCollectedToday(totalToday != null ? totalToday : BigDecimal.ZERO)
                .totalCollectedThisMonth(totalThisMonth != null ? totalThisMonth : BigDecimal.ZERO)
                .totalTransactionsToday(transactionsToday != null ? transactionsToday : 0L)
                .totalTransactionsThisMonth(transactionsThisMonth != null ? transactionsThisMonth : 0L)
                .pendingApprovals(pendingApprovals != null ? pendingApprovals : 0L)
                .collectionByPaymentMode(collectionByPaymentMode)
                .transactionsByStatus(transactionsByStatus)
                .averageTransactionAmount(avgAmount)
                .approvalRate(approvalRate)
                .build();
    }

    @Override
    @Cacheable(value = "otsDashboard", key = "'dashboard'")
    public DashboardDTO.OTSDashboard getOTSDashboard() {
        log.info("Building OTS dashboard");

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        Long totalRequests = otsRepository.count();
        Long pendingApprovals = otsRepository.countByOtsStatus(OTSStatus.PENDING_APPROVAL);
        Long approvedThisMonth = otsRepository.countByOtsStatusAndCreatedAtBetween(
                OTSStatus.APPROVED, startOfMonth, now);
        Long rejectedThisMonth = otsRepository.countByOtsStatusAndCreatedAtBetween(
                OTSStatus.REJECTED, startOfMonth, now);
        Long settledThisMonth = otsRepository.countByOtsStatusAndCreatedAtBetween(
                OTSStatus.SETTLED, startOfMonth, now);

        BigDecimal totalSettledAmount = otsRepository.sumSettledAmountByDateRange(startOfMonth, now);
        BigDecimal totalWaiverAmount = otsRepository.sumWaiverAmountByDateRange(startOfMonth, now);

        Double avgDiscount = otsRepository.averageDiscountPercentage();

        Map<String, Long> requestsByStatus = new HashMap<>();
        for (OTSStatus status : OTSStatus.values()) {
            requestsByStatus.put(status.name(), otsRepository.countByOtsStatus(status));
        }

        Long approved = requestsByStatus.getOrDefault("APPROVED", 0L) +
                requestsByStatus.getOrDefault("SETTLED", 0L) +
                requestsByStatus.getOrDefault("LETTER_GENERATED", 0L);
        Long total = requestsByStatus.values().stream().mapToLong(Long::longValue).sum();
        Double approvalRate = total > 0 ? (approved * 100.0) / total : 0.0;

        Long lettersGenerated = letterRepository.count();

        return DashboardDTO.OTSDashboard.builder()
                .totalRequests(totalRequests)
                .pendingApprovals(pendingApprovals != null ? pendingApprovals : 0L)
                .approvedThisMonth(approvedThisMonth != null ? approvedThisMonth : 0L)
                .rejectedThisMonth(rejectedThisMonth != null ? rejectedThisMonth : 0L)
                .settledThisMonth(settledThisMonth != null ? settledThisMonth : 0L)
                .totalSettledAmount(totalSettledAmount != null ? totalSettledAmount : BigDecimal.ZERO)
                .totalWaiverAmount(totalWaiverAmount != null ? totalWaiverAmount : BigDecimal.ZERO)
                .averageDiscountPercentage(avgDiscount != null ? avgDiscount : 0.0)
                .approvalRate(approvalRate)
                .requestsByStatus(requestsByStatus)
                .lettersGenerated(lettersGenerated)
                .build();
    }

    @Override
    public DashboardDTO.CollectionsDashboard getCollectionsDashboard() {
        log.info("Building complete collections dashboard");

        PTPStatsDTO ptpStats = ptpService.getPTPStats(null); // Get all stats without user filter

        return DashboardDTO.CollectionsDashboard.builder()
                .cycleClosureDashboard(getCycleClosureDashboard())
                .repaymentDashboard(getRepaymentDashboard())
                .otsDashboard(getOTSDashboard())
                .ptpStats(ptpStats)
                .build();
    }
}

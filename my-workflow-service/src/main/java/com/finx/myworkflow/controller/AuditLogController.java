package com.finx.myworkflow.controller;

import com.finx.myworkflow.domain.dto.AuditLogDTO;
import com.finx.myworkflow.domain.dto.CommonResponse;
import com.finx.myworkflow.domain.enums.AuditAction;
import com.finx.myworkflow.service.AuditLogService;
import com.finx.myworkflow.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<AuditLogDTO>> getAuditLog(@PathVariable Long id) {
        log.info("GET /audit/{} - Fetching audit log", id);
        AuditLogDTO response = auditLogService.getAuditLog(id);
        return ResponseWrapper.ok("Audit log retrieved successfully", response);
    }

    @GetMapping("/trail/{auditId}")
    public ResponseEntity<CommonResponse<AuditLogDTO>> getAuditLogByAuditId(@PathVariable String auditId) {
        log.info("GET /audit/trail/{} - Fetching audit log", auditId);
        AuditLogDTO response = auditLogService.getAuditLogByAuditId(auditId);
        return ResponseWrapper.ok("Audit log retrieved successfully", response);
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<CommonResponse<Page<AuditLogDTO>>> getCaseAuditTrail(
            @PathVariable Long caseId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("GET /audit/case/{} - Fetching case audit trail", caseId);
        Page<AuditLogDTO> auditLogs = auditLogService.getCaseAuditTrail(caseId, pageable);
        return ResponseWrapper.ok("Case audit trail retrieved successfully", auditLogs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CommonResponse<Page<AuditLogDTO>>> getUserAuditTrail(
            @PathVariable Long userId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("GET /audit/user/{} - Fetching user audit trail", userId);
        Page<AuditLogDTO> auditLogs = auditLogService.getUserAuditTrail(userId, pageable);
        return ResponseWrapper.ok("User audit trail retrieved successfully", auditLogs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<CommonResponse<Page<AuditLogDTO>>> getEntityAuditTrail(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("GET /audit/entity/{}/{} - Fetching entity audit trail", entityType, entityId);
        Page<AuditLogDTO> auditLogs = auditLogService.getEntityAuditTrail(entityType, entityId, pageable);
        return ResponseWrapper.ok("Entity audit trail retrieved successfully", auditLogs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<CommonResponse<Page<AuditLogDTO>>> getAuditLogsByAction(
            @PathVariable AuditAction action,
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("GET /audit/action/{} - Fetching audit logs", action);
        Page<AuditLogDTO> auditLogs = auditLogService.getAuditLogsByAction(action, pageable);
        return ResponseWrapper.ok("Audit logs retrieved successfully", auditLogs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<CommonResponse<Page<AuditLogDTO>>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("GET /audit/date-range - Fetching audit logs from {} to {}", startDate, endDate);
        Page<AuditLogDTO> auditLogs = auditLogService.getAuditLogsByDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("Audit logs retrieved successfully", auditLogs);
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<CommonResponse<List<AuditLogDTO>>> getRecentUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "24") int lastHours) {
        log.info("GET /audit/user/{}/recent - Fetching recent activity for last {} hours", userId, lastHours);
        List<AuditLogDTO> auditLogs = auditLogService.getRecentUserActivity(userId, lastHours);
        return ResponseWrapper.ok("Recent user activity retrieved successfully", auditLogs);
    }

    @GetMapping("/statistics")
    public ResponseEntity<CommonResponse<Map<String, Long>>> getActionStatistics(
            @RequestParam(defaultValue = "7") int lastDays) {
        log.info("GET /audit/statistics - Fetching action statistics for last {} days", lastDays);
        Map<String, Long> stats = auditLogService.getActionStatistics(lastDays);
        return ResponseWrapper.ok("Action statistics retrieved successfully", stats);
    }
}

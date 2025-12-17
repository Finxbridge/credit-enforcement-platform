package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.CommonResponse;
import com.finx.collectionsservice.domain.dto.DashboardDTO;
import com.finx.collectionsservice.service.DashboardService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/collections/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for collections dashboard and statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get complete dashboard", description = "Get complete collections dashboard with all stats")
    public ResponseEntity<CommonResponse<DashboardDTO.CollectionsDashboard>> getCompleteDashboard() {
        log.info("GET /dashboard - Fetching complete collections dashboard");
        DashboardDTO.CollectionsDashboard dashboard = dashboardService.getCollectionsDashboard();
        return ResponseWrapper.ok("Dashboard retrieved successfully", dashboard);
    }

    @GetMapping("/cycle-closure")
    @Operation(summary = "Get cycle closure dashboard", description = "Get cycle closure and archival statistics")
    public ResponseEntity<CommonResponse<DashboardDTO.CycleClosureDashboard>> getCycleClosureDashboard() {
        log.info("GET /dashboard/cycle-closure - Fetching cycle closure dashboard");
        DashboardDTO.CycleClosureDashboard dashboard = dashboardService.getCycleClosureDashboard();
        return ResponseWrapper.ok("Cycle closure dashboard retrieved successfully", dashboard);
    }

    @GetMapping("/repayments")
    @Operation(summary = "Get repayment dashboard", description = "Get repayment collection statistics")
    public ResponseEntity<CommonResponse<DashboardDTO.RepaymentDashboard>> getRepaymentDashboard() {
        log.info("GET /dashboard/repayments - Fetching repayment dashboard");
        DashboardDTO.RepaymentDashboard dashboard = dashboardService.getRepaymentDashboard();
        return ResponseWrapper.ok("Repayment dashboard retrieved successfully", dashboard);
    }

    @GetMapping("/ots")
    @Operation(summary = "Get OTS dashboard", description = "Get OTS request statistics")
    public ResponseEntity<CommonResponse<DashboardDTO.OTSDashboard>> getOTSDashboard() {
        log.info("GET /dashboard/ots - Fetching OTS dashboard");
        DashboardDTO.OTSDashboard dashboard = dashboardService.getOTSDashboard();
        return ResponseWrapper.ok("OTS dashboard retrieved successfully", dashboard);
    }
}

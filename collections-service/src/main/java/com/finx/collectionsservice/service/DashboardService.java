package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.DashboardDTO;

public interface DashboardService {

    DashboardDTO.CycleClosureDashboard getCycleClosureDashboard();

    DashboardDTO.RepaymentDashboard getRepaymentDashboard();

    DashboardDTO.OTSDashboard getOTSDashboard();

    DashboardDTO.CollectionsDashboard getCollectionsDashboard();
}

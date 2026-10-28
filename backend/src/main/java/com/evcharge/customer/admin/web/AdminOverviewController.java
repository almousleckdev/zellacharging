package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminOverviewService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.ReportingWindow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/overview")
class AdminOverviewController {

    private final AdminOverviewService overview;
    private final ReportingWindow reporting;

    AdminOverviewController(AdminOverviewService overview, ReportingWindow reporting) {
        this.overview = overview;
        this.reporting = reporting;
    }

    @GetMapping
    AdminOverviewResponse overview(AdminAccess admin, @RequestParam(required = false) String country) {
        return AdminOverviewResponse.from(overview.overview(admin, country), reporting.start());
    }
}

package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminOverviewService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/overview")
class AdminOverviewController {

    private final AdminOverviewService overview;

    AdminOverviewController(AdminOverviewService overview) {
        this.overview = overview;
    }

    @GetMapping
    AdminOverviewResponse overview(AdminAccess admin, @RequestParam(required = false) String country) {
        return AdminOverviewResponse.from(overview.overview(admin, country));
    }
}

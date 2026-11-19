package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminUsageService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/usage")
class AdminUsageController {

    private final AdminUsageService usage;

    AdminUsageController(AdminUsageService usage) {
        this.usage = usage;
    }

    @GetMapping
    UsageResponse summary(AdminAccess admin, @RequestParam(required = false) String country,
                          @RequestParam(required = false) String station,
                          @RequestParam(required = false) UUID customerId) {
        return UsageResponse.from(usage.summary(admin, country, station, customerId));
    }
}

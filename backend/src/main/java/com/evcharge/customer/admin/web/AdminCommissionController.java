package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminCommissionService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.LogFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/commissions")
class AdminCommissionController {

    private final AdminCommissionService commissions;

    AdminCommissionController(AdminCommissionService commissions) {
        this.commissions = commissions;
    }

    @GetMapping
    CommissionResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                            @RequestParam(required = false) String q, @RequestParam(required = false) Instant from,
                            @RequestParam(required = false) Instant to, @RequestParam(required = false) UUID customerId,
                            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return CommissionResponse.from(commissions.list(admin, country, q, new LogFilter(from, to, customerId), page,
                size));
    }
}

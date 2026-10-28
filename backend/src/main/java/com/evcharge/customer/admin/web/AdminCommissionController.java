package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminCommissionService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/commissions")
class AdminCommissionController {

    private final AdminCommissionService commissions;

    AdminCommissionController(AdminCommissionService commissions) {
        this.commissions = commissions;
    }

    @GetMapping
    CommissionResponse list(AdminAccess admin, @RequestParam(required = false) String country) {
        return CommissionResponse.from(commissions.list(admin, country));
    }
}

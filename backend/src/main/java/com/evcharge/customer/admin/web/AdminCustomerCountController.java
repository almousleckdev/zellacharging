package com.evcharge.customer.admin.web;

import com.evcharge.customer.account.CustomerCounts;
import com.evcharge.customer.admin.application.AdminCustomerCountService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/customer-counts")
class AdminCustomerCountController {

    private final AdminCustomerCountService counts;

    AdminCustomerCountController(AdminCustomerCountService counts) {
        this.counts = counts;
    }

    @GetMapping
    CustomerCounts counts(AdminAccess admin, @RequestParam(required = false) String country) {
        return counts.counts(admin, country);
    }
}

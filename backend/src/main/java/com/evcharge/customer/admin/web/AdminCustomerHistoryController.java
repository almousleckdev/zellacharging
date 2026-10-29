package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.ActivityListing;
import com.evcharge.customer.admin.application.AdminCustomerHistoryService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.LogFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/customers/{country}/{id}")
class AdminCustomerHistoryController {

    private final AdminCustomerHistoryService history;

    AdminCustomerHistoryController(AdminCustomerHistoryService history) {
        this.history = history;
    }

    @GetMapping("/ledger")
    LedgerListResponse ledger(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                              @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return LedgerListResponse.from(history.ledger(admin, country, id, new LogFilter(from, to, null), page, size),
                page, size);
    }

    @GetMapping("/activity")
    ActivityListing activity(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return history.activity(admin, country, id, page, size);
    }
}

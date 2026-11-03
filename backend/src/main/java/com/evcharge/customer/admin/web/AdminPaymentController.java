package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminSessionService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.LogFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/payments")
class AdminPaymentController {

    private static final String BILLED = "COMPLETED";

    private final AdminSessionService sessions;

    AdminPaymentController(AdminSessionService sessions) {
        this.sessions = sessions;
    }

    @GetMapping
    SessionListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                             @RequestParam(required = false) String q, @RequestParam(required = false) Instant from,
                             @RequestParam(required = false) Instant to, @RequestParam(required = false) UUID customerId,
                             @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return SessionListResponse.from(sessions.list(admin, Permission.PAYMENTS_READ, country, BILLED, q,
                new LogFilter(from, to, customerId), null, sort, page, size));
    }

    @GetMapping("/by-customer")
    SessionDigestResponse byCustomer(AdminAccess admin, @RequestParam(required = false) String country,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) Instant from,
                                     @RequestParam(required = false) Instant to,
                                     @RequestParam(required = false) String sort,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return SessionDigestResponse.from(sessions.byCustomer(admin, Permission.PAYMENTS_READ, country, BILLED, q,
                new LogFilter(from, to, null), null, sort, page, size));
    }

    @GetMapping("/{country}/{id}")
    SessionDetailResponse detail(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        return SessionDetailResponse.from(sessions.detail(admin, Permission.PAYMENTS_READ, country, id));
    }
}

package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminSessionService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.LogFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/sessions")
class AdminSessionController {

    private final AdminSessionService sessions;

    AdminSessionController(AdminSessionService sessions) {
        this.sessions = sessions;
    }

    @GetMapping
    SessionListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                             @RequestParam(required = false) String state, @RequestParam(required = false) String q,
                             @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
                             @RequestParam(required = false) UUID customerId, @RequestParam(required = false) String sort, @RequestParam(required = false) String station,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return SessionListResponse.from(sessions.list(admin, Permission.SESSIONS_READ, country, state, q, new LogFilter(from, to, customerId), station, sort, page, size));
    }

    @GetMapping("/by-customer")
    SessionDigestResponse byCustomer(AdminAccess admin, 
                                    @RequestParam(required = false) String country,
                                    @RequestParam(required = false) String state,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(required = false) Instant from,
                                    @RequestParam(required = false) Instant to,
                                    @RequestParam(required = false) String sort, 
                                    @RequestParam(required = false) String station,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return SessionDigestResponse.from(sessions.byCustomer(admin, Permission.SESSIONS_READ, country, state, q,
                new LogFilter(from, to, null), station, sort, page, size));
    }

    @GetMapping("/{country}/{id}")
    SessionDetailResponse detail(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        return SessionDetailResponse.from(sessions.detail(admin, Permission.SESSIONS_READ, country, id));
    }

    @PostMapping("/{country}/{id}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void stop(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        sessions.stop(admin, country, id);
    }
}

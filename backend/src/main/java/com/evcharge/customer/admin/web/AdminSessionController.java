package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminSessionService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return SessionListResponse.from(sessions.list(admin, country, state, q, page, size));
    }
}

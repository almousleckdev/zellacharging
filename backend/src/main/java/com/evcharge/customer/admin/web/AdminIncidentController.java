package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminIncidentService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.LogFilter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/admin/incidents")
class AdminIncidentController {

    private final AdminIncidentService incidents;

    AdminIncidentController(AdminIncidentService incidents) {
        this.incidents = incidents;
    }

    @GetMapping
    IncidentListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                              @RequestParam(required = false) String severity,
                              @RequestParam(required = false) String status, @RequestParam(required = false) String kind,
                              @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return IncidentListResponse.from(incidents.list(admin, country, severity, status, kind,
                new LogFilter(from, to, null), page, size));
    }

    @PostMapping("/{country}/review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void review(AdminAccess admin, @PathVariable String country, @Valid @RequestBody ReviewIncidentRequest request) {
        incidents.review(admin, country, request.key(), request.status(), request.note());
    }
}

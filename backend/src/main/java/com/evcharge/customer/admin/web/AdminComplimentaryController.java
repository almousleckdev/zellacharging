package com.evcharge.customer.admin.web;

import com.evcharge.customer.account.ComplimentaryCategory;
import com.evcharge.customer.admin.application.AdminComplimentaryService;
import com.evcharge.customer.admin.application.AdminFreeUsageService;
import com.evcharge.customer.admin.domain.AdminAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin")
class AdminComplimentaryController {

    private final AdminComplimentaryService complimentary;

    private final AdminFreeUsageService usage;

    AdminComplimentaryController(AdminComplimentaryService complimentary, AdminFreeUsageService usage) {
        this.complimentary = complimentary;
        this.usage = usage;
    }

    @GetMapping("/complimentary/usage")
    List<FreeUsageResponse> usage(AdminAccess admin, @RequestParam(required = false) String country,
                                  @RequestParam(required = false) ComplimentaryCategory category,
                                  @RequestParam(required = false) Instant from,
                                  @RequestParam(required = false) Instant to) {
        return usage.list(admin, country, category, from, to).stream().map(FreeUsageResponse::from).toList();
    }

    @GetMapping("/complimentary")
    List<ComplimentaryResponse> list(AdminAccess admin, @RequestParam(required = false) String country) {
        return complimentary.list(admin, country).stream().map(ComplimentaryResponse::from).toList();
    }

    @PutMapping("/customers/{country}/{id}/complimentary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void grant(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
               @Valid @RequestBody ComplimentaryRequest request) {
        complimentary.grant(admin, country, id, request.reason(), request.category(), request.expiresAt());
    }

    @DeleteMapping("/customers/{country}/{id}/complimentary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        complimentary.revoke(admin, country, id);
    }
}

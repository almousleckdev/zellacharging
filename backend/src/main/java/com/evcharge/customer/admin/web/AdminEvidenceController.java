package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminEvidenceService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/evidence")
class AdminEvidenceController {

    private final AdminEvidenceService evidence;

    AdminEvidenceController(AdminEvidenceService evidence) {
        this.evidence = evidence;
    }

    @GetMapping
    EvidenceListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return EvidenceListResponse.from(evidence.list(admin, country, page, size));
    }
}

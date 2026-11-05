package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.LookupService;
import com.evcharge.customer.partner.application.PartnerPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/partners/ndzela")
class LookupController {

    private final LookupService lookups;

    LookupController(LookupService lookups) {
        this.lookups = lookups;
    }

    @PostMapping("/id-tags/lookup")
    LookupResponse lookup(@AuthenticationPrincipal PartnerPrincipal principal, @RequestBody LookupRequest request) {
        return LookupResponse.of(lookups.lookup(principal, request.drivers()));
    }
}

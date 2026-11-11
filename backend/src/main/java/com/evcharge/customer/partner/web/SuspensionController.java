package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.PartnerPrincipal;
import com.evcharge.customer.partner.application.SuspensionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/partners/ndzela/customers/{customerId}/suspension")
class SuspensionController {

    private final SuspensionService suspensions;

    SuspensionController(SuspensionService suspensions) {
        this.suspensions = suspensions;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void suspend(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable UUID customerId,
                 @RequestBody SuspensionRequest request) {
        suspensions.suspend(principal, customerId, request.owedMinor(), request.currency(), request.decimals());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void restore(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable UUID customerId) {
        suspensions.restore(principal, customerId);
    }
}

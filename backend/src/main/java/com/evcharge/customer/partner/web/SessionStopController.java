package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.PartnerPrincipal;
import com.evcharge.customer.partner.application.PartnerStopService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/partners/ndzela/sessions")
class SessionStopController {

    private final PartnerStopService stops;

    SessionStopController(PartnerStopService stops) {
        this.stops = stops;
    }

    @PostMapping("/{transactionId}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void stop(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable int transactionId) {
        stops.stop(principal, transactionId);
    }
}

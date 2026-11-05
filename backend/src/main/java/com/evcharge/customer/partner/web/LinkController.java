package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.LinkService;
import com.evcharge.customer.partner.application.PartnerPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/partners/ndzela/links")
class LinkController {

    private final LinkService links;

    LinkController(LinkService links) {
        this.links = links;
    }

    @PutMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void link(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable UUID customerId) {
        links.link(principal, customerId);
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlink(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable UUID customerId) {
        links.unlink(principal, customerId);
    }
}

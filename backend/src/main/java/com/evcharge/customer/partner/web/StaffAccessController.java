package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.PartnerPrincipal;
import com.evcharge.customer.partner.application.StaffAccessService;
import jakarta.validation.Valid;
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
@RequestMapping("/v1/partners/ndzela/staff-access")
class StaffAccessController {

    private final StaffAccessService staff;

    StaffAccessController(StaffAccessService staff) {
        this.staff = staff;
    }

    @PutMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void grant(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable UUID customerId,
               @Valid @RequestBody StaffAccessRequest request) {
        staff.grant(principal, customerId, request.reason());
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(@AuthenticationPrincipal PartnerPrincipal principal, @PathVariable UUID customerId) {
        staff.revoke(principal, customerId);
    }
}

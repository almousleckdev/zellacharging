package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminInvitationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/auth/invitation")
class AdminInvitationController {

    private final AdminInvitationService invitations;

    AdminInvitationController(AdminInvitationService invitations) {
        this.invitations = invitations;
    }

    @GetMapping
    InvitationInfoResponse inspect(@RequestParam String token) {
        return new InvitationInfoResponse(invitations.inspect(token));
    }

    @PostMapping
    AdminLoginResponse accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return AdminLoginResponse.from(invitations.accept(request.token(), request.password()));
    }
}

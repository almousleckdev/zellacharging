package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.InvitationResult;

record InvitationResponse(AdminUserResponse admin, boolean emailSent) {

    static InvitationResponse from(InvitationResult result) {
        return new InvitationResponse(AdminUserResponse.from(result.admin()), result.emailSent());
    }
}

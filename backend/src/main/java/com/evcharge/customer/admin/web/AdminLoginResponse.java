package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminLoginStep;

record AdminLoginResponse(String stage, String challengeToken, String secret, String provisioningUri) {

    static AdminLoginResponse from(AdminLoginStep step) {
        return new AdminLoginResponse(step.stage().name(), step.challengeToken(), step.secret(),
                step.provisioningUri());
    }
}

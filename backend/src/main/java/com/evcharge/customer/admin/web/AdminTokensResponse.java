package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminTokens;

record AdminTokensResponse(String accessToken, String refreshToken, long expiresIn) {

    static AdminTokensResponse from(AdminTokens tokens) {
        return new AdminTokensResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
    }
}

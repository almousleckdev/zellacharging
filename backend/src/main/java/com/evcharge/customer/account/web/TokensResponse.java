package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AuthTokens;

record TokensResponse(String accessToken, String refreshToken, long expiresIn) {

    static TokensResponse from(AuthTokens tokens) {
        return new TokensResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
    }
}

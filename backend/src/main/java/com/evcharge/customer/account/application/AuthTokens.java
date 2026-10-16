package com.evcharge.customer.account.application;

public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}

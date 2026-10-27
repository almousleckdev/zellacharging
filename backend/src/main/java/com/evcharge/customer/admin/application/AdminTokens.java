package com.evcharge.customer.admin.application;

public record AdminTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}

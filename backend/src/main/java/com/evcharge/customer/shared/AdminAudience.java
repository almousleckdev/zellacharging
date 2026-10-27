package com.evcharge.customer.shared;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public final class AdminAudience {

    public static final String ACCESS = "zella-admin";
    public static final String CHALLENGE = "zella-admin-challenge";
    public static final String PATH_PREFIX = "/v1/admin";

    private AdminAudience() {
    }

    public static boolean isAccessToken(Jwt jwt) {
        return hasAudience(jwt, ACCESS);
    }

    public static boolean isChallengeToken(Jwt jwt) {
        return hasAudience(jwt, CHALLENGE);
    }

    private static boolean hasAudience(Jwt jwt, String audience) {
        List<String> audiences = jwt.getAudience();
        return audiences != null && audiences.contains(audience);
    }
}

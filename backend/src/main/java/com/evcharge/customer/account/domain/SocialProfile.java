package com.evcharge.customer.account.domain;

public record SocialProfile(
        SocialProvider provider,
        String subject,
        String email,
        boolean emailVerified,
        String firstName,
        String lastName) {
}

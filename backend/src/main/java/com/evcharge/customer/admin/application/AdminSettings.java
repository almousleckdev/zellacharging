package com.evcharge.customer.admin.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.admin")
public record AdminSettings(
        String bootstrapEmail,
        String bootstrapPassword,
        String totpKey,
        String totpIssuer,
        String mailFrom,
        String dashboardUrl,
        Duration invitationTtl,
        Duration challengeTtl,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        int maxFailedLogins,
        Duration lockout) {
}

package com.evcharge.customer.account.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("zella.social")
record SocialSettings(
        List<String> googleClientIds,
        List<String> appleClientIds,
        String appleTeamId,
        String appleKeyId,
        String applePrivateKey) {

    boolean appleRevocationConfigured() {
        return !appleClientIds.isEmpty() && notBlank(appleTeamId) && notBlank(appleKeyId)
                && notBlank(applePrivateKey);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}

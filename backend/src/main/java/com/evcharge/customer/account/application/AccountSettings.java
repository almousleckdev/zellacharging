package com.evcharge.customer.account.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella")
public record AccountSettings(SecuritySettings security, VerificationSettings verification, String mailFrom) {
}

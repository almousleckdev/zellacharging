package com.evcharge.customer.shared.tenancy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.tenancy")
public record TenancySettings(String defaultCountry, boolean requireRestrictedRole, Duration countryCacheTtl) {
}

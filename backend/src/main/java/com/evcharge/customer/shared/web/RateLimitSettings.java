package com.evcharge.customer.shared.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella.rate-limit")
record RateLimitSettings(
        int loginPerIpPerMinute,
        int registerPerIpPerHour,
        int resendPerIpPerHour,
        int verifyPerIpPerMinute,
        int refreshPerIpPerMinute,
        int callbackPerIpPerMinute,
        int topupPerCustomerPerMinute,
        int sessionStartPerCustomerPerMinute,
        int withdrawalPerCustomerPerMinute) {
}

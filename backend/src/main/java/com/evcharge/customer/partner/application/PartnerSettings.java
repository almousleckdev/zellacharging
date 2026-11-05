package com.evcharge.customer.partner.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.partner")
public record PartnerSettings(int ratePerMinute, int maxLookupBatch, int defaultFeedLimit, int maxFeedLimit,
                              Duration feedLag) {
}

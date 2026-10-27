package com.evcharge.customer.shared.tenancy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TenancySettings.class)
class TenancyConfiguration {
}

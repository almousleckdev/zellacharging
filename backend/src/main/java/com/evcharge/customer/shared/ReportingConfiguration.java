package com.evcharge.customer.shared;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReportingWindow.class)
class ReportingConfiguration {
}

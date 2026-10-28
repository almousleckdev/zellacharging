package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.StallSettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({SessionSettings.class, FeeSettings.class, StallSettings.class})
class ChargingConfiguration {
}

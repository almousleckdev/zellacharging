package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.PartnerSettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PartnerSettings.class, NdzelaSettings.class})
class PartnerConfiguration {
}

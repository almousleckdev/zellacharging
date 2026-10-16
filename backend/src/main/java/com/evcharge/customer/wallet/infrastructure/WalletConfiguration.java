package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.application.WalletSettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WalletSettings.class)
class WalletConfiguration {
}

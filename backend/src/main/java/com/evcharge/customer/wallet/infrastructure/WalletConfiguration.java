package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.application.PaymentSettings;
import com.evcharge.customer.wallet.application.WalletSettings;
import com.evcharge.customer.wallet.application.WithdrawalSettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WalletSettings.class, PaymentSettings.class, WithdrawalSettings.class})
class WalletConfiguration {
}

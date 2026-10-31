package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.application.PaymentSettings;
import com.evcharge.customer.wallet.application.WalletDefaults;
import com.evcharge.customer.wallet.application.WithdrawalDefaults;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WalletDefaults.class, PaymentSettings.class, WithdrawalDefaults.class})
class WalletConfiguration {
}

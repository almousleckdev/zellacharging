package com.evcharge.customer.wallet;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class ScriptedGatewayConfiguration {

    @Bean
    ScriptedPaymentGateway scriptedPaymentGateway() {
        return new ScriptedPaymentGateway();
    }

    @Bean
    ScriptedPayoutGateway scriptedPayoutGateway() {
        return new ScriptedPayoutGateway();
    }
}

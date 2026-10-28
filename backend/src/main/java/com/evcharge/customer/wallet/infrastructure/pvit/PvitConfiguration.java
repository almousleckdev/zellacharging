package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.domain.MobileOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(PvitProperties.class)
@ConditionalOnProperty(name = "zella.payment.provider", havingValue = "pvit")
class PvitConfiguration {

    private static final String AIRTEL_ACCOUNT = "!'${zella.payment.pvit.compte-operation-airtel:}'.isEmpty()";
    private static final String MOOV_ACCOUNT = "!'${zella.payment.pvit.compte-operation-moov:}'.isEmpty()";
    private static final String PAYOUT = " and !'${zella.payment.pvit.code-url-payout:}'.isEmpty()";

    @Bean
    PvitWiring pvitWiring(PvitProperties properties, ObjectMapper mapper) {
        return new PvitWiring(properties, mapper);
    }

    @Bean
    @ConditionalOnExpression(AIRTEL_ACCOUNT)
    PvitGateway pvitAirtelGateway(PvitWiring wiring) {
        return wiring.collection(MobileOperator.AIRTEL);
    }

    @Bean
    @ConditionalOnExpression(MOOV_ACCOUNT)
    PvitGateway pvitMoovGateway(PvitWiring wiring) {
        return wiring.collection(MobileOperator.MOOV);
    }

    @Bean
    @ConditionalOnExpression(AIRTEL_ACCOUNT + PAYOUT)
    PvitPayoutGateway pvitAirtelPayoutGateway(PvitWiring wiring) {
        return wiring.payout(MobileOperator.AIRTEL);
    }

    @Bean
    @ConditionalOnExpression(MOOV_ACCOUNT + PAYOUT)
    PvitPayoutGateway pvitMoovPayoutGateway(PvitWiring wiring) {
        return wiring.payout(MobileOperator.MOOV);
    }
}

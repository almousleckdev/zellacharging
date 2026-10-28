package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.domain.MobileOperator;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Optional;

@ConfigurationProperties("zella.payment.pvit")
record PvitProperties(
        String baseUrl,
        String compteOperationAirtel,
        String compteOperationMoov,
        String secretPassword,
        String codeUrlSecret,
        String codeUrlPayment,
        String codeUrlStatus,
        String codeUrlPayout,
        String codeUrlKyc,
        String callbackUrlCode,
        String agent,
        String product,
        String ownerCharge,
        Duration requestTimeout) {

    Optional<String> accountOf(MobileOperator operator) {
        String account = switch (operator) {
            case AIRTEL -> compteOperationAirtel;
            case MOOV -> compteOperationMoov;
        };
        return Optional.ofNullable(account).filter(code -> !code.isBlank());
    }

    @Override
    public String toString() {
        return "PvitProperties[secrets hidden]";
    }
}

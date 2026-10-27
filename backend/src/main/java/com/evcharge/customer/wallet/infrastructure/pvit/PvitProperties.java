package com.evcharge.customer.wallet.infrastructure.pvit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.payment.pvit")
record PvitProperties(
        String baseUrl,
        String operationAccountCode,
        String secretPassword,
        String codeUrlSecret,
        String codeUrlPayment,
        String codeUrlStatus,
        String codeUrlPayout,
        String callbackUrlCode,
        String agent,
        String product,
        String ownerCharge,
        Duration requestTimeout) {

    @Override
    public String toString() {
        return "PvitProperties[secrets hidden]";
    }
}

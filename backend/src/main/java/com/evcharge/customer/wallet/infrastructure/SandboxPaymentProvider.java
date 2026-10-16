package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.application.port.PaymentProvider;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import com.evcharge.customer.wallet.domain.Topup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ndzela.payment.provider", havingValue = "sandbox")
class SandboxPaymentProvider implements PaymentProvider {

    @Override
    public PaymentOutcome collect(Topup topup, String currency) {
        return PaymentOutcome.succeeded("sandbox-" + topup.id());
    }
}

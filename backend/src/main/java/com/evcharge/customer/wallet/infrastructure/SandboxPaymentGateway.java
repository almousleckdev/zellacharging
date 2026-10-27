package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.application.port.PaymentGateway;
import com.evcharge.customer.wallet.domain.CollectionRequest;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "zella.payment.provider", havingValue = "sandbox")
class SandboxPaymentGateway implements PaymentGateway {

    @Override
    public boolean supports(String country, MobileOperator operator) {
        return true;
    }

    @Override
    public PaymentOutcome collect(CollectionRequest request) {
        return PaymentOutcome.succeeded("sandbox-" + request.reference(), request.amountMinor());
    }

    @Override
    public PaymentOutcome status(String reference) {
        return PaymentOutcome.pending(null);
    }
}

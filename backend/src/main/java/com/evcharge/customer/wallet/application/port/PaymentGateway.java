package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.CollectionRequest;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;

public interface PaymentGateway {

    boolean supports(String country, MobileOperator operator);

    PaymentOutcome collect(CollectionRequest request);

    PaymentOutcome status(String reference);
}

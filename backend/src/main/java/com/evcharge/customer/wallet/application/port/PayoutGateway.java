package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutRequest;

public interface PayoutGateway {

    boolean supports(String country, MobileOperator operator);

    PayoutOutcome payout(PayoutRequest request);

    PayoutOutcome status(String reference);
}

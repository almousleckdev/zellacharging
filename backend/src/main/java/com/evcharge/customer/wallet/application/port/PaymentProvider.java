package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.PaymentOutcome;
import com.evcharge.customer.wallet.domain.Topup;

public interface PaymentProvider {

    PaymentOutcome collect(Topup topup, String currency);
}

package com.evcharge.customer.wallet;

import java.util.Optional;
import java.util.UUID;

public interface PaymentLog {

    PaymentPage search(PaymentQuery query);

    Optional<PaymentDetail> find(String kind, UUID id);
}

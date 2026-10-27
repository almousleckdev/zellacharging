package com.evcharge.customer.account;

import java.util.UUID;

public interface CustomerVerification {

    String verifiedPhone(UUID customerId);

    void confirmPassword(UUID customerId, String password);
}

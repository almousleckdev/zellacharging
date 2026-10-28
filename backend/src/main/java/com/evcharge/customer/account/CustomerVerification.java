package com.evcharge.customer.account;

import java.util.UUID;

public interface CustomerVerification {

    void confirmPassword(UUID customerId, String password);
}

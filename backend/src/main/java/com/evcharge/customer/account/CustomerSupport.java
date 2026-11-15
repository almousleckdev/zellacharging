package com.evcharge.customer.account;

import java.util.UUID;

public interface CustomerSupport {

    CustomerSupportState state(UUID customerId);

    void resendCode(UUID customerId, String channel, String delivery);

    void resetCodeAttempts(UUID customerId, String channel);

    void unlockLogin(UUID customerId);

    void markVerified(UUID customerId, String channel);

    void changeContact(UUID customerId, String channel, String value);
}

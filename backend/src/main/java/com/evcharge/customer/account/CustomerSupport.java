package com.evcharge.customer.account;

import java.util.UUID;

public interface CustomerSupport {

    CustomerSupportState state(UUID customerId);

    void resendCode(UUID customerId, String channel, String delivery);

    void resetCodeAttempts(UUID customerId, String channel);

    void unlockLogin(UUID customerId);

    void setPassword(UUID customerId, String newPassword);

    void markVerified(UUID customerId, String channel);

    void changeName(UUID customerId, String firstName, String lastName);

    void changeContact(UUID customerId, String channel, String value);
}

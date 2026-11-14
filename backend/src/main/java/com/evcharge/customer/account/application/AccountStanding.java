package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CustomerStanding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class AccountStanding implements CustomerStanding {

    private final CustomerRepository customers;
    private final SuspendedRefusal refusal;

    AccountStanding(CustomerRepository customers, SuspendedRefusal refusal) {
        this.customers = customers;
        this.refusal = refusal;
    }

    @Override
    @Transactional(readOnly = true)
    public void requireActive(UUID customerId) {
        boolean suspended = customers.findById(customerId)
                .map(customer -> customer.status() == CustomerStatus.BLOCKED).orElse(false);
        if (suspended) {
            throw refusal.of(customerId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void requireVerifiedContacts(UUID customerId) {
        boolean complete = customers.findById(customerId)
                .map(customer -> customer.email() != null && customer.phone() != null).orElse(false)
                && customers.isVerified(customerId, Channel.EMAIL) && customers.isVerified(customerId, Channel.PHONE);
        if (!complete) {
            throw new ApiException(HttpStatus.FORBIDDEN, "CONTACTS_NOT_VERIFIED",
                    "Verify your email and phone number before charging");
        }
    }
}

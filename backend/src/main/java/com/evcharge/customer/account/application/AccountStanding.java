package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.CustomerStanding;
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
}

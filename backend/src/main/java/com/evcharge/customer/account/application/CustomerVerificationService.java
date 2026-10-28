package com.evcharge.customer.account.application;

import com.evcharge.customer.account.CustomerVerification;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class CustomerVerificationService implements CustomerVerification {

    private final CustomerRepository customers;
    private final PasswordConfirmation passwords;

    CustomerVerificationService(CustomerRepository customers, PasswordConfirmation passwords) {
        this.customers = customers;
        this.passwords = passwords;
    }

    @Override
    public void confirmPassword(UUID customerId, String password) {
        passwords.confirm(customerId, password);
    }
}

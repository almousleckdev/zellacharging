package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileService {

    private final CustomerRepository customers;

    public ProfileService(CustomerRepository customers) {
        this.customers = customers;
    }

    public Customer get(UUID customerId) {
        return customers.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found"));
    }
}

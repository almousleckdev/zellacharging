package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordService {

    private final ProfileService profiles;
    private final CustomerRepository customers;
    private final PasswordHasher passwords;

    public PasswordService(ProfileService profiles, CustomerRepository customers, PasswordHasher passwords) {
        this.profiles = profiles;
        this.customers = customers;
        this.passwords = passwords;
    }

    public void change(UUID customerId, String currentPassword, String newPassword) {
        confirm(customerId, currentPassword);
        customers.updatePasswordHash(customerId, passwords.hash(newPassword));
    }

    Customer confirm(UUID customerId, String password) {
        Customer customer = profiles.get(customerId);
        if (!passwords.matches(password, customer.passwordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WRONG_PASSWORD", "The password is incorrect");
        }
        return customer;
    }
}

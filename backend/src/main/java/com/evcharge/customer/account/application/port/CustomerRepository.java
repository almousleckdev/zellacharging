package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.Customer;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    void save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByContact(Channel channel, String target);

    void markContactVerified(UUID id, Channel channel, Instant at);

    void recordFailedLogin(UUID id, int attempts, Instant lockedUntil);

    void clearFailedLogins(UUID id);

    void updateName(UUID id, String firstName, String lastName);

    void updatePasswordHash(UUID id, String passwordHash);

    void markDeleted(UUID id, String unusablePasswordHash);
}

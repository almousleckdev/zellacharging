package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.Customer;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    void save(Customer customer);
    Optional<Customer> findById(UUID id);
    boolean isVerified(UUID id, Channel channel);
    Optional<Customer> findByContact(Channel channel, String target);
    void markContactVerified(UUID id, Channel channel, Instant at);
    void registerFailedLogin(UUID id, int maxFailures, Instant lockedUntil);
    void clearFailedLogins(UUID id);
    void updateName(UUID id, String firstName, String lastName);
    void updatePasswordHash(UUID id, String passwordHash);
    boolean updateStatus(UUID id, String from, String to);
    void markDeleted(UUID id, String unusablePasswordHash);
}

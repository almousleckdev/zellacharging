package com.evcharge.customer.account.application;

import com.evcharge.customer.account.ClosureBlocker;
import com.evcharge.customer.account.CustomerModeration;
import com.evcharge.customer.account.application.port.AvatarRepository;
import com.evcharge.customer.account.application.port.CustomerIdentityRepository;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class AccountModerationService implements CustomerModeration {

    private final CustomerRepository customers;
    private final RefreshTokenRepository refreshTokens;
    private final AvatarRepository avatars;
    private final CustomerIdentityRepository identities;
    private final PasswordHasher hasher;
    private final List<ClosureBlocker> blockers;
    private final Clock clock;

    AccountModerationService(CustomerRepository customers, RefreshTokenRepository refreshTokens,
                             AvatarRepository avatars, CustomerIdentityRepository identities,
                             PasswordHasher hasher, List<ClosureBlocker> blockers, Clock clock) {
        this.customers = customers;
        this.refreshTokens = refreshTokens;
        this.avatars = avatars;
        this.identities = identities;
        this.hasher = hasher;
        this.blockers = blockers;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean suspended(UUID customerId) {
        return load(customerId).status() == CustomerStatus.BLOCKED;
    }

    @Override
    @Transactional
    public void suspend(UUID customerId) {
        Customer customer = load(customerId);
        if (customer.status() == CustomerStatus.BLOCKED) {
            throw conflict("CUSTOMER_ALREADY_SUSPENDED");
        }
        if (!customers.updateStatus(customerId, customer.status().name(), CustomerStatus.BLOCKED.name())) {
            throw conflict("CUSTOMER_STATE_CHANGED");
        }
        refreshTokens.revokeAllOf(customerId, clock.instant());
    }

    @Override
    @Transactional
    public void reactivate(UUID customerId) {
        Customer customer = load(customerId);
        if (customer.status() != CustomerStatus.BLOCKED) {
            throw conflict("CUSTOMER_NOT_SUSPENDED");
        }
        customers.updateStatus(customerId, CustomerStatus.BLOCKED.name(), CustomerStatus.ACTIVE.name());
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public void delete(UUID customerId) {
        load(customerId);
        blockers.stream().map(blocker -> blocker.blockingCode(customerId)).flatMap(Optional::stream).findFirst()
                .ifPresent(code -> {
                    throw conflict(code);
                });
        customers.markDeleted(customerId, hasher.hash(UUID.randomUUID().toString()));
        refreshTokens.revokeAllOf(customerId, clock.instant());
        avatars.delete(customerId);
        identities.deleteOf(customerId);
    }

    private Customer load(UUID customerId) {
        return customers.findById(customerId).filter(customer -> customer.status() != CustomerStatus.DELETED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND",
                        "Customer not found"));
    }

    private static ApiException conflict(String code) {
        return new ApiException(HttpStatus.CONFLICT, code, "The customer cannot be changed");
    }
}

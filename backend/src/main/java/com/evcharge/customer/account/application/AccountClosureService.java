package com.evcharge.customer.account.application;

import com.evcharge.customer.account.ClosureBlocker;
import com.evcharge.customer.account.application.port.AvatarRepository;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class AccountClosureService {

    private final PasswordService passwords;
    private final PasswordHasher hasher;
    private final CustomerRepository customers;
    private final RefreshTokenRepository refreshTokens;
    private final AvatarRepository avatars;
    private final List<ClosureBlocker> blockers;
    private final Clock clock;

    public AccountClosureService(PasswordService passwords, PasswordHasher hasher, CustomerRepository customers,
                                 RefreshTokenRepository refreshTokens, AvatarRepository avatars,
                                 List<ClosureBlocker> blockers, Clock clock) {
        this.passwords = passwords;
        this.hasher = hasher;
        this.customers = customers;
        this.refreshTokens = refreshTokens;
        this.avatars = avatars;
        this.blockers = blockers;
        this.clock = clock;
    }

    @Transactional
    public void close(UUID customerId, String password) {
        passwords.confirm(customerId, password);
        blockers.stream().map(blocker -> blocker.blockingCode(customerId)).flatMap(java.util.Optional::stream)
                .findFirst().ifPresent(code -> {
                    throw new ApiException(HttpStatus.CONFLICT, code, "The account cannot be closed yet");
                });
        customers.markDeleted(customerId, hasher.hash(UUID.randomUUID().toString()));
        refreshTokens.revokeAllOf(customerId, clock.instant());
        avatars.delete(customerId);
    }
}

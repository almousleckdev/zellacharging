package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.SocialProvider;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CustomerIdentityRepository {

    Optional<UUID> customerOf(SocialProvider provider, String subject);

    boolean isLinked(UUID customerId, SocialProvider provider, String subject);

    void link(UUID customerId, SocialProvider provider, String subject, Instant at);

    void deleteOf(UUID customerId);
}

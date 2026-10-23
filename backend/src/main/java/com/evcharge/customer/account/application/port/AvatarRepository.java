package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.Avatar;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AvatarRepository {

    void save(UUID customerId, Avatar avatar, Instant at);

    Optional<Avatar> find(UUID customerId);

    void delete(UUID customerId);
}

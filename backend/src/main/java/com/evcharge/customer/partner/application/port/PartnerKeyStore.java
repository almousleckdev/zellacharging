package com.evcharge.customer.partner.application.port;

import com.evcharge.customer.partner.domain.PartnerKey;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerKeyStore {

    void insert(PartnerKey key);

    Optional<PartnerKey> findByHash(String keyHash);

    Optional<PartnerKey> findById(UUID id);

    void revoke(UUID id, Instant at);

    void touch(UUID id, Instant at);

    List<PartnerKey> list(String partner);
}

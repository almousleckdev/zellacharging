package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.IssuedKey;
import com.evcharge.customer.partner.PartnerKeyInfo;
import com.evcharge.customer.partner.PartnerKeys;
import com.evcharge.customer.partner.application.port.PartnerKeyStore;
import com.evcharge.customer.partner.domain.PartnerKey;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PartnerKeyService implements PartnerKeys {

    private static final String PREFIX = "zpk_";
    private static final int KEY_BYTES = 32;
    private static final Duration TOUCH_EVERY = Duration.ofMinutes(1);

    private final SecureRandom random = new SecureRandom();
    private final PartnerKeyStore store;
    private final AuditTrail audit;
    private final Clock clock;

    public PartnerKeyService(PartnerKeyStore store, AuditTrail audit, Clock clock) {
        this.store = store;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public IssuedKey issue(String partner, String country, UUID actorId) {
        byte[] bytes = new byte[KEY_BYTES];
        random.nextBytes(bytes);
        String raw = PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        UUID id = UUID.randomUUID();
        store.insert(new PartnerKey(id, partner, country, hash(raw), clock.instant(), null, null, null));
        audited(country, "PARTNER_KEY_ISSUED", actorId, id, partner);
        return new IssuedKey(id, raw);
    }

    @Override
    public void revoke(UUID keyId, UUID actorId) {
        PartnerKey key = store.findById(keyId).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Key not found"));
        store.revoke(keyId, clock.instant());
        audited(key.country(), "PARTNER_KEY_REVOKED", actorId, keyId, key.partner());
    }

    @Override
    public List<PartnerKeyInfo> list(String partner) {
        return store.list(partner).stream().map(key -> new PartnerKeyInfo(key.id(), key.partner(), key.country(),
                key.createdAt(), key.expiresAt(), key.revokedAt(), key.lastUsedAt())).toList();
    }

    public Optional<PartnerPrincipal> authenticate(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        return store.findByHash(hash(rawKey)).filter(key -> key.usableAt(now)).map(key -> {
            if (key.lastUsedAt() == null || key.lastUsedAt().plus(TOUCH_EVERY).isBefore(now)) {
                store.touch(key.id(), now);
            }
            return new PartnerPrincipal(key.id(), key.partner(), key.country());
        });
    }

    private void audited(String country, String action, UUID actorId, UUID keyId, String partner) {
        TenantContext.runAs(country, () -> audit.record(action, actorId, keyId.toString(), partner + ":" + country));
    }

    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.application.PartnerKeyService;
import com.evcharge.customer.partner.application.PartnerPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PartnerKeyServiceIntegrationTest extends PostgresTestBase {

    @Autowired
    PartnerKeyService service;

    @Autowired
    JdbcTemplate jdbc;

    private final UUID actor = UUID.randomUUID();

    @Test
    void issuedKeyAuthenticatesAndIsStoredOnlyAsHash() {
        IssuedKey issued = service.issue("NDZELA", "GA", actor);

        assertThat(service.authenticate(issued.key())).map(PartnerPrincipal::keyId).contains(issued.id());
        String stored = jdbc.queryForObject("select key_hash from partner_api_key where id = ?", String.class,
                issued.id());
        assertThat(stored).isNotEqualTo(issued.key()).doesNotContain(issued.key());
        Integer rawCopies = jdbc.queryForObject("select count(*) from partner_api_key where key_hash = ?",
                Integer.class, issued.key());
        assertThat(rawCopies).isZero();
    }

    @Test
    void unknownKeyDoesNotAuthenticate() {
        assertThat(service.authenticate("zpk_not-a-real-key")).isEmpty();
        assertThat(service.authenticate("")).isEmpty();
        assertThat(service.authenticate(null)).isEmpty();
    }

    @Test
    void revokedKeyStopsAuthenticating() {
        IssuedKey issued = service.issue("NDZELA", "GA", actor);

        service.revoke(issued.id(), actor);

        assertThat(service.authenticate(issued.key())).isEmpty();
    }

    @Test
    void expiredKeyStopsAuthenticating() {
        IssuedKey issued = service.issue("NDZELA", "GA", actor);
        new PartnerFixtures(jdbc, service).expire(issued.id());

        assertThat(service.authenticate(issued.key())).isEmpty();
    }

    @Test
    void twoActiveKeysWorkIndependentlyAndRevokingOneKeepsTheOther() {
        IssuedKey first = service.issue("NDZELA", "GA", actor);
        IssuedKey second = service.issue("NDZELA", "GA", actor);

        assertThat(service.authenticate(first.key())).isPresent();
        assertThat(service.authenticate(second.key())).isPresent();
        service.revoke(first.id(), actor);

        assertThat(service.authenticate(first.key())).isEmpty();
        assertThat(service.authenticate(second.key())).isPresent();
    }

    @Test
    void authenticatingRecordsLastUsedAt() {
        IssuedKey issued = service.issue("NDZELA", "GA", actor);
        assertThat(lastUsed(issued.id())).isNull();

        service.authenticate(issued.key());

        assertThat(lastUsed(issued.id())).isNotNull();
    }

    @Test
    void keysCarryTheirCountry() {
        IssuedKey gabon = service.issue("NDZELA", "GA", actor);
        IssuedKey mali = service.issue("NDZELA", "ML", actor);

        assertThat(service.authenticate(gabon.key())).map(PartnerPrincipal::country).contains("GA");
        assertThat(service.authenticate(mali.key())).map(PartnerPrincipal::country).contains("ML");
    }

    @Test
    void listNeverExposesTheKeyOrItsHash() {
        IssuedKey issued = service.issue("NDZELA", "GA", actor);

        List<PartnerKeyInfo> listed = service.list("NDZELA");

        assertThat(listed).extracting(PartnerKeyInfo::id).contains(issued.id());
        assertThat(listed.toString()).doesNotContain(issued.key());
    }

    private Object lastUsed(UUID id) {
        return jdbc.queryForObject("select last_used_at from partner_api_key where id = ?", Object.class, id);
    }
}

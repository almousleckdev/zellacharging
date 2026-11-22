package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.partner.PartnerFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcFreeSundayWindowsTest extends PostgresTestBase {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);
    private static final Instant END = Instant.parse("2031-06-01T21:00:00Z");

    @Autowired
    FreeSundayClaims claims;

    @Autowired
    JdbcTemplate jdbc;

    private PartnerFixtures fixtures;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc);
    }

    private UUID customer() {
        return fixtures.customer(TENANT, "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
    }

    private UUID claimed(UUID customer, UUID session, Duration age) {
        claims.claim(customer, SUNDAY, session, UUID.randomUUID(), END);
        jdbc.update("update free_sunday_claim set claimed_at = now() - make_interval(secs => ?) "
                + "where session_id = ?", age.toSeconds(), session);
        return session;
    }

    private String stateOf(UUID session) {
        return jdbc.queryForObject("select state from free_sunday_claim where session_id = ?", String.class, session);
    }

    private Instant aDayAgo() {
        return databaseNowMinus("1 day");
    }

    private Instant twoMinutesAgo() {
        return databaseNowMinus("2 minutes");
    }

    private Instant databaseNowMinus(String interval) {
        return jdbc.queryForObject("select now() - cast(? as interval)", Timestamp.class, interval).toInstant();
    }

    @Test
    void releasesAClaimWithoutSessionClaimedFiveMinutesAgo() {
        UUID orphan = claimed(customer(), UUID.randomUUID(), Duration.ofMinutes(5));

        assertThat(claims.releaseOrphans(aDayAgo(), twoMinutesAgo())).contains(orphan);

        assertThat(stateOf(orphan)).isEqualTo("RELEASED");
    }

    @Test
    void keepsAClaimWithoutSessionClaimedThirtySecondsAgo() {
        UUID fresh = claimed(customer(), UUID.randomUUID(), Duration.ofSeconds(30));

        assertThat(claims.releaseOrphans(aDayAgo(), twoMinutesAgo())).doesNotContain(fresh);

        assertThat(stateOf(fresh)).isEqualTo("CLAIMED");
    }

    @Test
    void neverReleasesAClaimWhoseSessionExists() {
        UUID customer = customer();
        UUID session = fixtures.session(customer, "COMPLETED", null, Instant.now().minusSeconds(3600));
        claimed(customer, session, Duration.ofHours(1));

        assertThat(claims.releaseOrphans(aDayAgo(), twoMinutesAgo())).doesNotContain(session);

        assertThat(stateOf(session)).isEqualTo("CLAIMED");
    }

    @Test
    void keepsAClaimWhoseSessionStillHasAnOpenHold() {
        UUID customer = customer();
        UUID held = claimed(customer, UUID.randomUUID(), Duration.ofMinutes(5));
        jdbc.update("insert into funding_hold (session_id, customer_id, reserved_minor, state, waived, created_at) "
                + "values (?, ?, 200000, 'OPEN', true, now())", held, customer);

        assertThat(claims.releaseOrphans(aDayAgo(), twoMinutesAgo())).doesNotContain(held);

        assertThat(stateOf(held)).isEqualTo("CLAIMED");
    }

    @Test
    void ignoresClaimsOlderThanTheLookBack() {
        UUID old = claimed(customer(), UUID.randomUUID(), Duration.ofDays(2));

        assertThat(claims.releaseOrphans(aDayAgo(), twoMinutesAgo())).doesNotContain(old);

        assertThat(stateOf(old)).isEqualTo("CLAIMED");
    }

    @Test
    void windowsAreReadFromTheEndInclusiveAndOnlyAfterTheLookBack() {
        UUID atEnd = claimed(customer(), UUID.randomUUID(), Duration.ZERO);
        UUID older = UUID.randomUUID();
        claims.claim(customer(), SUNDAY, older, UUID.randomUUID(), END.minus(Duration.ofDays(2)));

        assertThat(claims.sessionsPastWindow(END.minus(Duration.ofDays(1)), END.minusSeconds(1)))
                .doesNotContain(atEnd, older);
        assertThat(claims.sessionsPastWindow(END.minus(Duration.ofDays(1)), END)).contains(atEnd)
                .doesNotContain(older);
    }
}

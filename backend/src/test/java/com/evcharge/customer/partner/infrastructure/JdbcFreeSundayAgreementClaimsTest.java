package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.partner.PartnerFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcFreeSundayAgreementClaimsTest extends PostgresTestBase {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);

    @Autowired
    FreeSundayClaims claims;

    @Autowired
    JdbcTemplate jdbc;

    private final UUID agreement = UUID.randomUUID();
    private UUID first;
    private UUID second;
    private Instant inOneHour;

    @BeforeEach
    void twoCustomersOfOneDriver() {
        PartnerFixtures fixtures = new PartnerFixtures(jdbc);
        first = fixtures.customer(TENANT, "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        second = fixtures.customer(TENANT, "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        inOneHour = Instant.now().plus(1, ChronoUnit.HOURS);
    }

    private boolean claim(UUID customer, UUID session, UUID agreementId) {
        return claims.claim(customer, SUNDAY, session, agreementId, inOneHour);
    }

    @Test
    void aSecondCustomerOfTheSameAgreementCannotClaimTheSameSunday() {
        assertThat(claim(first, UUID.randomUUID(), agreement)).isTrue();
        assertThat(claim(second, UUID.randomUUID(), agreement)).isFalse();
    }

    @Test
    void isClaimedSeesTheSundayClaimedByAnotherCustomerOfTheAgreement() {
        claim(first, UUID.randomUUID(), agreement);
        assertThat(claims.isClaimed(agreement, second, SUNDAY)).isTrue();
        assertThat(claims.isClaimed(UUID.randomUUID(), second, SUNDAY)).isFalse();
    }

    @Test
    void isClaimedStillSeesTheCustomersOwnClaimUnderAnotherAgreement() {
        claim(first, UUID.randomUUID(), agreement);
        assertThat(claims.isClaimed(UUID.randomUUID(), first, SUNDAY)).isTrue();
    }

    @Test
    void aReleasedClaimFreesTheSundayForEitherCustomer() {
        UUID session = UUID.randomUUID();
        claim(first, session, agreement);
        claims.release(session);
        assertThat(claims.isClaimed(agreement, second, SUNDAY)).isFalse();
        assertThat(claim(second, UUID.randomUUID(), agreement)).isTrue();
        assertThat(claims.isClaimed(agreement, first, SUNDAY)).isTrue();
    }

    @Test
    void theAgreementCanStillBeUsedOnAnotherSunday() {
        claim(first, UUID.randomUUID(), agreement);
        assertThat(claims.claim(second, SUNDAY.plusDays(7), UUID.randomUUID(), agreement, inOneHour)).isTrue();
    }
}

package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.PartnerFixtures;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerContactsIntegrationTest extends PostgresTestBase {

    @Autowired
    CustomerContacts contacts;

    @Autowired
    JdbcTemplate jdbc;

    private PartnerFixtures fixtures() {
        return new PartnerFixtures(jdbc);
    }

    private static String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private static String phone() {
        return "+2417" + String.format("%07d", (int) (Math.random() * 10_000_000));
    }

    @Test
    void findsByEmailOnly() {
        String email = unique("c") + "@example.com";
        UUID id = fixtures().customer("GA", "Ada", "Lovelace", email, null, "ACTIVE");

        List<CustomerMatch> found = contacts.activeByEmailOrPhone(List.of(email), List.of());

        assertThat(found).extracting(CustomerMatch::id).containsExactly(id);
        assertThat(found.get(0).firstName()).isEqualTo("Ada");
    }

    @Test
    void findsByPhoneOnly() {
        String phone = phone();
        UUID id = fixtures().customer("GA", "Ada", "Lovelace", unique("p") + "@example.com", phone, "ACTIVE");

        assertThat(contacts.activeByEmailOrPhone(List.of(), List.of(phone))).extracting(CustomerMatch::id)
                .containsExactly(id);
    }

    @Test
    void findsByEmailAndPhoneTogetherWithoutDuplicates() {
        String email = unique("b") + "@example.com";
        String phone = phone();
        UUID id = fixtures().customer("GA", "Ada", "Lovelace", email, phone, "ACTIVE");

        assertThat(contacts.activeByEmailOrPhone(List.of(email), List.of(phone))).extracting(CustomerMatch::id)
                .containsExactly(id);
    }

    @Test
    void ignoresBlockedAndPendingCustomers() {
        String blocked = unique("x") + "@example.com";
        String pending = unique("y") + "@example.com";
        fixtures().customer("GA", "A", "B", blocked, null, "BLOCKED");
        fixtures().customer("GA", "A", "B", pending, null, "PENDING_PHONE");

        assertThat(contacts.activeByEmailOrPhone(List.of(blocked, pending), List.of())).isEmpty();
    }

    @Test
    void doesNotCrossCountriesAndActiveExistsIsFalseForOtherCountry() {
        String email = unique("m") + "@example.com";
        UUID mali = fixtures().customer("ML", "A", "B", email, null, "ACTIVE");

        assertThat(contacts.activeByEmailOrPhone(List.of(email), List.of())).isEmpty();
        assertThat(contacts.activeExists(mali)).isFalse();
        TenantContext.runAs("ML", () -> assertThat(contacts.activeExists(mali)).isTrue());
    }

    @Test
    void emptyInputsReturnNothing() {
        assertThat(contacts.activeByEmailOrPhone(List.of(), List.of())).isEmpty();
    }
}

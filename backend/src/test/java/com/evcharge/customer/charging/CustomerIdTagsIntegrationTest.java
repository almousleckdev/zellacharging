package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.PartnerFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerIdTagsIntegrationTest extends PostgresTestBase {

    @Autowired
    CustomerIdTags idTags;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void returnsOnlyExistingTagsAndNeverCreatesOne() {
        PartnerFixtures fixtures = new PartnerFixtures(jdbc);
        UUID charged = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        UUID never = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        String tag = fixtures.idTagFor("GA", charged);
        Integer before = jdbc.queryForObject("select count(*) from customer_id_tag", Integer.class);

        var found = idTags.existing(List.of(charged, never));

        assertThat(found).containsEntry(charged, tag).doesNotContainKey(never);
        assertThat(jdbc.queryForObject("select count(*) from customer_id_tag", Integer.class)).isEqualTo(before);
    }

    @Test
    void emptyInputReturnsEmptyMap() {
        assertThat(idTags.existing(List.of())).isEmpty();
    }
}

package com.evcharge.customer.coupon;

import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CouponIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private UUID customer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        return id;
    }

    private void coupon(UUID customer, String title, String expires, String used) {
        jdbc.update("insert into coupon (id, customer_id, title, expires_at, used_at, created_at) values "
                + "(?, ?, ?, " + expires + ", " + used + ", now())", UUID.randomUUID(), customer, title);
    }

    @Test
    void onlyTheCustomersUsableCouponsAreListed() throws Exception {
        UUID mine = customer();
        UUID other = customer();
        coupon(mine, "Valid forever", "null", "null");
        coupon(mine, "Valid until next year", "now() + interval '365 days'", "null");
        coupon(mine, "Expired", "now() - interval '1 day'", "null");
        coupon(mine, "Already used", "null", "now()");
        coupon(other, "Someone else's", "null", "null");

        mvc.perform(get("/v1/coupons").with(jwt().jwt(j -> j.subject(mine.toString()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.title == 'Expired')]").isEmpty())
                .andExpect(jsonPath("$[?(@.title == 'Already used')]").isEmpty())
                .andExpect(jsonPath("$[?(@.title == 'Someone else\\'s')]").isEmpty());
    }

    @Test
    void aCustomerWithoutCouponsGetsAnEmptyList() throws Exception {
        mvc.perform(get("/v1/coupons").with(jwt().jwt(j -> j.subject(customer().toString()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }
}

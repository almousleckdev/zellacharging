package com.evcharge.customer.notification;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.notification.application.NotificationDispatcher;
import com.evcharge.customer.notification.application.port.PushSender;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.notification.domain.PushResult;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationInboxIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    Notifier notifier;

    @Autowired
    NotificationDispatcher dispatcher;

    @MockitoBean
    PushSender sender;

    private UUID customer;

    @BeforeEach
    void newCustomer() {
        reset(sender);
        when(sender.available()).thenReturn(true);
        jdbc.update("delete from notification_outbox");
        jdbc.update("delete from device_token");
        customer = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, email_verified_at, password_hash, "
                + "status, created_at) values (?, 'A', 'B', ?, now(), 'x', 'ACTIVE', now())", customer,
                customer + "@example.com");
    }

    private void queue(String key, NotificationType type) {
        notifier.notify(customer, type, key, Map.of("screen", "wallet", "id", "w1"));
    }

    @Test
    void listsTheCustomersNotificationsInTheirLanguageWithTheUnreadCount() throws Exception {
        queue("a", NotificationType.TOPUP_SUCCEEDED);
        queue("b", NotificationType.PASSWORD_CHANGED);

        mvc.perform(get("/v1/notifications").with(TestTenants.customer(customer)).header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread", is(2)))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[?(@.type=='TOPUP_SUCCEEDED')].title").value("Top-up successful"))
                .andExpect(jsonPath("$.items[?(@.type=='TOPUP_SUCCEEDED')].screen").value("wallet"))
                .andExpect(jsonPath("$.items[?(@.type=='TOPUP_SUCCEEDED')].entityId").value("w1"));
        mvc.perform(get("/v1/notifications").with(TestTenants.customer(customer)))
                .andExpect(jsonPath("$.items[?(@.type=='TOPUP_SUCCEEDED')].title").value("Recharge réussie"));
    }

    @Test
    void anotherCustomerSeesNothingOfIt() throws Exception {
        queue("a", NotificationType.TOPUP_SUCCEEDED);

        mvc.perform(get("/v1/notifications").with(TestTenants.customer(UUID.randomUUID())))
                .andExpect(jsonPath("$.unread", is(0)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void markingOneReadLowersTheCountAndOnlyTheOwnerCanDoIt() throws Exception {
        queue("a", NotificationType.TOPUP_SUCCEEDED);
        queue("b", NotificationType.PASSWORD_CHANGED);
        String id = jdbc.queryForObject("select id::text from notification_outbox where dedupe_key = 'a'",
                String.class);

        mvc.perform(post("/v1/notifications/{id}/read", id).with(TestTenants.customer(UUID.randomUUID())))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/notifications/unread-count").with(TestTenants.customer(customer)))
                .andExpect(jsonPath("$.count", is(2)));

        mvc.perform(post("/v1/notifications/{id}/read", id).with(TestTenants.customer(customer)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/notifications/unread-count").with(TestTenants.customer(customer)))
                .andExpect(jsonPath("$.count", is(1)));
    }

    @Test
    void markingAllReadClearsTheCount() throws Exception {
        queue("a", NotificationType.TOPUP_SUCCEEDED);
        queue("b", NotificationType.PASSWORD_CHANGED);

        mvc.perform(post("/v1/notifications/read").with(TestTenants.customer(customer)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/v1/notifications").with(TestTenants.customer(customer)))
                .andExpect(jsonPath("$.unread", is(0)))
                .andExpect(jsonPath("$.items[0].read", is(true)));
    }

    @Test
    void anonymousCallersAreRejected() throws Exception {
        mvc.perform(get("/v1/notifications")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/notifications/unread-count")).andExpect(status().isUnauthorized());
        mvc.perform(post("/v1/notifications/read")).andExpect(status().isUnauthorized());
    }

    @Test
    void everyPushCarriesTheNumberOfUnreadNotificationsAsTheIconBadge() throws Exception {
        mvc.perform(put("/v1/devices").with(TestTenants.customer(customer)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"tok\",\"platform\":\"IOS\",\"language\":\"fr\"}"))
                .andExpect(status().isNoContent());
        when(sender.send(any(), any())).thenReturn(PushResult.DELIVERED);
        queue("a", NotificationType.TOPUP_SUCCEEDED);
        queue("b", NotificationType.PASSWORD_CHANGED);

        dispatcher.run();

        ArgumentCaptor<PushContent> contents = ArgumentCaptor.forClass(PushContent.class);
        verify(sender, times(2)).send(any(), contents.capture());
        assertThat(contents.getAllValues()).extracting(PushContent::badge).containsOnly(2);
    }
}

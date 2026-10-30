package com.evcharge.customer.notification;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.notification.application.NotificationDispatcher;
import com.evcharge.customer.notification.application.port.PushSender;
import com.evcharge.customer.notification.domain.DeviceToken;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"zella.notifications.max-attempts=2", "zella.notifications.retry-base=0s"})
class NotificationIntegrationTest extends PostgresTestBase {

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
        customer = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, email_verified_at, password_hash, "
                + "status, created_at) values (?, 'A', 'B', ?, now(), 'x', 'ACTIVE', now())", customer,
                customer + "@example.com");
    }

    private void register(String token, String platform, String language) throws Exception {
        mvc.perform(put("/v1/devices").with(TestTenants.customer(customer)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"%s\",\"platform\":\"%s\",\"language\":\"%s\"}".formatted(token, platform,
                        language))).andExpect(status().isNoContent());
    }

    private void queue(String key) {
        notifier.notify(customer, NotificationType.TOPUP_SUCCEEDED, key, Map.of("screen", "wallet"));
    }

    private String stateOf(String key) {
        return jdbc.queryForObject("select state from notification_outbox where customer_id = ? and dedupe_key = ?",
                String.class, customer, key);
    }

    @Test
    void aNotificationReachesEveryDeviceInItsOwnLanguage() throws Exception {
        register("tok-fr", "ANDROID", "fr");
        register("tok-en", "IOS", "en");
        when(sender.send(any(), any())).thenReturn(PushResult.DELIVERED);
        queue("k1");

        dispatcher.run();

        ArgumentCaptor<DeviceToken> devices = ArgumentCaptor.forClass(DeviceToken.class);
        ArgumentCaptor<PushContent> contents = ArgumentCaptor.forClass(PushContent.class);
        verify(sender, times(2)).send(devices.capture(), contents.capture());
        assertThat(contents.getAllValues()).extracting(PushContent::title)
                .containsExactlyInAnyOrder("Recharge réussie", "Top-up successful");
        assertThat(contents.getValue().data()).containsEntry("type", "TOPUP_SUCCEEDED")
                .containsEntry("screen", "wallet");
        assertThat(stateOf("k1")).isEqualTo("SENT");
    }

    @Test
    void theSameEventIsQueuedOnlyOnce() {
        queue("dup");
        queue("dup");

        assertThat(jdbc.queryForObject("select count(*) from notification_outbox where customer_id = ?",
                Integer.class, customer)).isOne();
    }

    @Test
    void anInvalidTokenIsRemovedAndNotRetried() throws Exception {
        register("dead-token", "ANDROID", "fr");
        when(sender.send(any(), any())).thenReturn(PushResult.INVALID_TOKEN);
        queue("k2");

        dispatcher.run();

        assertThat(jdbc.queryForObject("select count(*) from device_token where customer_id = ?", Integer.class,
                customer)).isZero();
        assertThat(stateOf("k2")).isEqualTo("SENT");
    }

    @Test
    void aFailingDeliveryIsRetriedThenGivenUp() throws Exception {
        register("slow-token", "ANDROID", "fr");
        when(sender.send(any(), any())).thenReturn(PushResult.RETRY);
        queue("k3");

        dispatcher.run();
        assertThat(stateOf("k3")).isEqualTo("PENDING");
        dispatcher.run();

        assertThat(stateOf("k3")).isEqualTo("DEAD");
    }

    @Test
    void aCustomerWithoutDevicesStillGetsTheEventRecorded() {
        queue("k4");

        dispatcher.run();

        verify(sender, never()).send(any(), any());
        assertThat(stateOf("k4")).isEqualTo("SENT");
    }

    @Test
    void aDeviceCanBeUnregisteredOnlyByItsOwner() throws Exception {
        register("mine", "ANDROID", "fr");

        mvc.perform(delete("/v1/devices").with(TestTenants.anyCustomer()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"mine\",\"platform\":\"ANDROID\"}")).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from device_token where token = 'mine'", Integer.class))
                .isOne();

        mvc.perform(delete("/v1/devices").with(TestTenants.customer(customer)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"mine\",\"platform\":\"ANDROID\"}")).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from device_token where token = 'mine'", Integer.class))
                .isZero();
    }
}

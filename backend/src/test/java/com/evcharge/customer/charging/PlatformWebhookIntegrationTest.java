package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.PartnerFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "zella.ocpp-webhook.secret=whsec-it")
class PlatformWebhookIntegrationTest extends PostgresTestBase {

    private static final String SECRET = "whsec-it";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private static String sign(String text) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static String metering(String eventId, String station, String idTag) {
        return "{\"eventId\":\"" + eventId + "\",\"eventType\":\"session.metering\",\"data\":{\"deviceId\":\""
                + station + "\",\"connectorId\":1,\"transactionId\":5,\"idTag\":\"" + idTag
                + "\",\"sampledAt\":\"2026-11-05T18:00:00Z\",\"totalKwh\":4.2,\"currentPowerKw\":30.5,"
                + "\"voltage\":400.1,\"currentAmpere\":75.0,\"stateOfChargePercent\":93,\"durationSeconds\":600}}";
    }

    private UUID liveSession(String station, String idTag) {
        UUID customer = new PartnerFixtures(jdbc).customer("GA", "W", "H", UUID.randomUUID() + "@example.com", null,
                "ACTIVE");
        UUID id = UUID.randomUUID();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "transaction_id, started_at, currency, currency_decimals, created_at, updated_at) "
                + "values (?, ?, ?, 1, ?, 'ACTIVE', 5, ?, 'XOF', 0, ?, ?)", id, customer, station, idTag, now, now,
                now);
        return id;
    }

    private Map<String, Object> row(UUID id) {
        return jdbc.queryForMap("select last_energy_kwh, battery_percent, last_voltage, last_power_kw "
                + "from charging_session where id = ?", id);
    }

    @Test
    void aSignedMeteringEventUpdatesTheLiveReadingsOfTheSession() throws Exception {
        UUID id = liveSession("CP-WH-1", "Z-WH-1");
        String body = metering("evt-" + UUID.randomUUID(), "CP-WH-1", "Z-WH-1");
        long timestamp = Instant.now().getEpochSecond();

        mvc.perform(post("/v1/ocpp/webhooks").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Webhook-Signature", sign(timestamp + "\n" + body))
                        .header("X-Webhook-Signature-Timestamp", Long.toString(timestamp)))
                .andExpect(status().isAccepted());

        Map<String, Object> stored = waitForReading(id);
        assertThat((BigDecimal) stored.get("last_energy_kwh")).isEqualByComparingTo("4.2");
        assertThat((BigDecimal) stored.get("battery_percent")).isEqualByComparingTo("93");
        assertThat((BigDecimal) stored.get("last_voltage")).isEqualByComparingTo("400.1");
        assertThat((BigDecimal) stored.get("last_power_kw")).isEqualByComparingTo("30.5");
    }

    @Test
    void theBodyOnlySignatureIsAcceptedToo() throws Exception {
        UUID id = liveSession("CP-WH-2", "Z-WH-2");
        String body = metering("evt-" + UUID.randomUUID(), "CP-WH-2", "Z-WH-2");

        mvc.perform(post("/v1/ocpp/webhooks").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Webhook-Signature", sign(body)))
                .andExpect(status().isAccepted());

        assertThat(waitForReading(id).get("battery_percent")).isNotNull();
    }

    @Test
    void aWrongSignatureIsRefusedAndChangesNothing() throws Exception {
        UUID id = liveSession("CP-WH-3", "Z-WH-3");
        String body = metering("evt-" + UUID.randomUUID(), "CP-WH-3", "Z-WH-3");

        mvc.perform(post("/v1/ocpp/webhooks").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Webhook-Signature", "0".repeat(64)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("WEBHOOK_SIGNATURE_INVALID"));

        assertThat(row(id).get("battery_percent")).isNull();
    }

    @Test
    void aMissingSignatureIsRefused() throws Exception {
        mvc.perform(post("/v1/ocpp/webhooks").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aSignedBodyThatIsNotAnEventIsRejectedAsMalformed() throws Exception {
        String body = "{\"hello\":\"world\"}";

        mvc.perform(post("/v1/ocpp/webhooks").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Webhook-Signature", sign(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void deviceEventsAreAcknowledgedAndIgnored() throws Exception {
        String body = "{\"eventId\":\"evt-" + UUID.randomUUID() + "\",\"eventType\":\"device.online\","
                + "\"data\":{\"deviceId\":\"CP-WH-4\"}}";

        mvc.perform(post("/v1/ocpp/webhooks").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Webhook-Signature", sign(body)))
                .andExpect(status().isAccepted());
    }

    private Map<String, Object> waitForReading(UUID id) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            Map<String, Object> current = row(id);
            if (current.get("battery_percent") != null) {
                return current;
            }
            Thread.sleep(100);
        }
        return row(id);
    }
}

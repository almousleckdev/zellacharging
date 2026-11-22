package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.shared.FreeSundayWindow;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class HttpNdzelaFreeChargingTest {

    private static final String KEY = "partner-key";

    private final UUID customer = UUID.randomUUID();
    private final UUID agreement = UUID.randomUUID();
    private final AtomicReference<String> seenKey = new AtomicReference<>();
    private final AtomicReference<String> seenPath = new AtomicReference<>();
    private HttpServer server;
    private int status;
    private String body;
    private long delayMillis;

    @BeforeEach
    void startServer() throws IOException {
        status = 200;
        body = "";
        delayMillis = 0;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            seenKey.set(exchange.getRequestHeaders().getFirst("X-Api-Key"));
            seenPath.set(exchange.getRequestURI().getPath());
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            try {
                exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
                if (bytes.length > 0) {
                    exchange.getResponseBody().write(bytes);
                }
            } catch (IOException ignored) {
            }
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private HttpNdzelaFreeCharging client(Duration readTimeout) {
        String url = "http://127.0.0.1:" + server.getAddress().getPort();
        return new HttpNdzelaFreeCharging(new NdzelaRestClient(new NdzelaSettings(url, KEY),
                Duration.ofSeconds(1), readTimeout));
    }

    @Test
    void eligibleAnswerIsParsedAndTheKeyIsSent() {
        body = "{\"eligible\":true,\"reason\":\"ELIGIBLE\",\"agreementId\":\"" + agreement
                + "\",\"sundayDate\":\"2026-11-22\",\"windowEndsAt\":\"2026-11-22T21:00:00Z\"}";
        FreeChargingEligibility answer = client(Duration.ofSeconds(2)).check(customer);
        assertThat(answer.eligible()).isTrue();
        assertThat(answer.reason()).isEqualTo("ELIGIBLE");
        assertThat(answer.agreementId()).isEqualTo(agreement);
        assertThat(answer.sundayDate()).isEqualTo(LocalDate.of(2026, 11, 22));
        assertThat(answer.windowEndsAt()).isEqualTo(Instant.parse("2026-11-22T21:00:00Z"));
        assertThat(seenKey.get()).isEqualTo(KEY);
        assertThat(seenPath.get()).isEqualTo("/api/v1/partners/zella/customers/" + customer + "/free-charging");
    }

    @Test
    void theWindowOfTheAnswerIsParsedWhetherOrNotTheDriverIsEligible() {
        body = "{\"eligible\":false,\"reason\":\"OUTSIDE_WINDOW\",\"agreementId\":null,\"sundayDate\":null,"
                + "\"windowEndsAt\":null,\"window\":{\"day\":\"SATURDAY\",\"from\":\"08:00\",\"until\":\"23:59\","
                + "\"endOfDay\":true}}";
        FreeChargingEligibility answer = client(Duration.ofSeconds(2)).check(customer);
        assertThat(answer.window()).isEqualTo(new FreeSundayWindow("SATURDAY", "08:00", "23:59", true));
    }

    @Test
    void anAnswerWithoutAWindowStillWorks() {
        body = "{\"eligible\":false,\"reason\":\"NOT_LINKED\"}";
        assertThat(client(Duration.ofSeconds(2)).check(customer).window()).isNull();
    }

    @Test
    void notEligibleAnswerKeepsTheReason() {
        body = "{\"eligible\":false,\"reason\":\"UNPAID_DAYS\",\"agreementId\":null,\"sundayDate\":null,"
                + "\"windowEndsAt\":null}";
        FreeChargingEligibility answer = client(Duration.ofSeconds(2)).check(customer);
        assertThat(answer.eligible()).isFalse();
        assertThat(answer.reason()).isEqualTo("UNPAID_DAYS");
        assertThat(answer.agreementId()).isNull();
    }

    @Test
    void timeoutGivesUNAVAILABLE() {
        delayMillis = 1500;
        body = "{\"eligible\":true,\"reason\":\"ELIGIBLE\"}";
        assertThat(client(Duration.ofMillis(300)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void errorStatusGivesUNAVAILABLE() {
        status = 500;
        body = "{\"error\":\"boom\"}";
        assertThat(client(Duration.ofSeconds(2)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void notFoundGivesUNAVAILABLE() {
        status = 404;
        assertThat(client(Duration.ofSeconds(2)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void malformedJsonGivesUNAVAILABLE() {
        body = "{not json";
        assertThat(client(Duration.ofSeconds(2)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void eligibleWithoutAgreementDetailsGivesUNAVAILABLE() {
        body = "{\"eligible\":true,\"reason\":\"ELIGIBLE\"}";
        assertThat(client(Duration.ofSeconds(2)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void missingConfigurationGivesUNAVAILABLE() {
        HttpNdzelaFreeCharging unconfigured = new HttpNdzelaFreeCharging(new NdzelaRestClient(
                new NdzelaSettings("", ""), Duration.ofSeconds(1), Duration.ofSeconds(1)));
        assertThat(unconfigured.check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void eligibleWithAnotherReasonGivesUNAVAILABLE() {
        body = "{\"eligible\":true,\"reason\":\"NOT_LINKED\",\"agreementId\":\"" + agreement
                + "\",\"sundayDate\":\"2026-11-22\",\"windowEndsAt\":\"2026-11-22T21:00:00Z\"}";
        assertThat(client(Duration.ofSeconds(2)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
    }

    @Test
    void redirectIsNotFollowedAndTheKeyNeverReachesAnotherHost() throws IOException {
        AtomicReference<String> otherKey = new AtomicReference<>();
        AtomicReference<Boolean> otherHit = new AtomicReference<>(false);
        HttpServer other = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        other.createContext("/", exchange -> {
            otherHit.set(true);
            otherKey.set(exchange.getRequestHeaders().getFirst("X-Api-Key"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        other.start();
        try {
            server.removeContext("/");
            server.createContext("/", exchange -> {
                exchange.getResponseHeaders().add("Location", "http://127.0.0.1:" + other.getAddress().getPort() + "/x");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
            });
            assertThat(client(Duration.ofSeconds(2)).check(customer)).isEqualTo(FreeChargingEligibility.unavailable());
            assertThat(otherHit.get()).isFalse();
            assertThat(otherKey.get()).isNull();
        } finally {
            other.stop(0);
        }
    }
}

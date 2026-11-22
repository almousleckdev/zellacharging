package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.AgreementTerms;
import com.evcharge.customer.partner.FreeAgreement;
import com.evcharge.customer.partner.FreeChargingUsePage;
import com.evcharge.customer.shared.ApiException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpFreeChargingAdministrationTest {

    private static final String KEY = "partner-key-secret";
    private static final String BASE = "/api/v1/partners/zella/free-charging";

    private final UUID agreement = UUID.randomUUID();
    private final UUID driver = UUID.randomUUID();
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> target = new AtomicReference<>();
    private final AtomicReference<String> key = new AtomicReference<>();
    private final AtomicReference<String> actor = new AtomicReference<>();
    private final AtomicReference<String> sent = new AtomicReference<>();
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
            method.set(exchange.getRequestMethod());
            target.set(exchange.getRequestURI().toString());
            key.set(exchange.getRequestHeaders().getFirst("X-Api-Key"));
            actor.set(exchange.getRequestHeaders().getFirst("X-Actor"));
            sent.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
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

    private HttpFreeChargingAdministration client(Duration readTimeout) {
        String url = "http://127.0.0.1:" + server.getAddress().getPort();
        return new HttpFreeChargingAdministration(new NdzelaRestClient(new NdzelaSettings(url, KEY),
                Duration.ofSeconds(1), readTimeout));
    }

    private HttpFreeChargingAdministration client() {
        return client(Duration.ofSeconds(2));
    }

    private String agreementJson() {
        return "{\"id\":\"" + agreement + "\",\"driverId\":\"" + driver + "\",\"driverName\":\"Ada Obiang\","
                + "\"startsOn\":\"2026-11-01\",\"endsOn\":null,\"status\":\"ACTIVE\",\"conditions\":\"none\","
                + "\"eligibility\":\"OUTSIDE_WINDOW\",\"unpaidDays\":2,\"lastFreeUse\":\"2026-11-15T13:00:00Z\"}";
    }

    @Test
    void theListIsReadWithTheKeyAndTheActor() {
        body = "[" + agreementJson() + "]";
        List<FreeAgreement> rows = client().list("admin@zella.test");
        assertThat(method.get()).isEqualTo("GET");
        assertThat(target.get()).isEqualTo(BASE + "/agreements");
        assertThat(key.get()).isEqualTo(KEY);
        assertThat(actor.get()).isEqualTo("admin@zella.test");
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().driverName()).isEqualTo("Ada Obiang");
        assertThat(rows.getFirst().eligibility()).isEqualTo("OUTSIDE_WINDOW");
        assertThat(rows.getFirst().unpaidDays()).isEqualTo(2);
        assertThat(rows.getFirst().startsOn()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(rows.getFirst().lastFreeUse()).isNotNull();
    }

    @Test
    void createPostsTheTermsAsJson() {
        status = 201;
        body = agreementJson();
        FreeAgreement created = client().create("root", driver,
                new AgreementTerms(LocalDate.of(2026, 11, 1), null, "none"));
        assertThat(method.get()).isEqualTo("POST");
        assertThat(target.get()).isEqualTo(BASE + "/agreements");
        assertThat(sent.get()).contains("\"customerId\":\"" + driver + "\"").doesNotContain("driverId").contains("\"startsOn\":\"2026-11-01\"")
                .contains("\"conditions\":\"none\"");
        assertThat(actor.get()).isEqualTo("root");
        assertThat(created.id()).isEqualTo(agreement);
    }

    @Test
    void updatePutsTheTerms() {
        body = agreementJson();
        client().update("root", agreement, new AgreementTerms(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 1), null));
        assertThat(method.get()).isEqualTo("PUT");
        assertThat(target.get()).isEqualTo(BASE + "/agreements/" + agreement);
        assertThat(sent.get()).contains("\"endsOn\":\"2027-01-01\"").doesNotContain("driverId").doesNotContain("customerId");
    }

    @Test
    void suspendResumeAndRevokePostToTheirOwnRoutes() {
        body = agreementJson();
        client().suspend("root", agreement);
        assertThat(method.get()).isEqualTo("POST");
        assertThat(target.get()).isEqualTo(BASE + "/agreements/" + agreement + "/suspend");
        client().resume("root", agreement);
        assertThat(target.get()).isEqualTo(BASE + "/agreements/" + agreement + "/resume");
        client().revoke("root", agreement);
        assertThat(target.get()).isEqualTo(BASE + "/agreements/" + agreement + "/revoke");
        assertThat(actor.get()).isEqualTo("root");
    }

    @Test
    void usageIsPagedAndParsed() {
        body = "{\"items\":[{\"sessionId\":\"" + UUID.randomUUID() + "\",\"driverId\":\"" + driver
                + "\",\"driverName\":\"Ada\",\"startedAt\":\"2026-11-15T13:00:00Z\",\"energyKwh\":12.5,"
                + "\"stopReason\":\"FREE_WINDOW_ENDED\"}],\"page\":1,\"size\":10,\"total\":11}";
        FreeChargingUsePage page = client().usage("root", 1, 10);
        assertThat(target.get()).isEqualTo(BASE + "/usage?page=1&size=10");
        assertThat(page.total()).isEqualTo(11);
        assertThat(page.items().getFirst().energyKwh()).isEqualByComparingTo("12.5");
        assertThat(page.items().getFirst().stopReason()).isEqualTo("FREE_WINDOW_ENDED");
    }

    @Test
    void anNdzelaBusinessRefusalKeepsItsCodeAndStatus() {
        status = 422;
        body = "{\"code\":\"FREE_AGREEMENT_EXISTS\",\"message\":\"internal wording\"}";
        assertThatThrownBy(() -> client().suspend("root", agreement)).isInstanceOfSatisfying(ApiException.class, e -> {
            assertThat(e.status().value()).isEqualTo(422);
            assertThat(e.code()).isEqualTo("FREE_AGREEMENT_EXISTS");
            assertThat(e.getMessage()).doesNotContain("internal wording").doesNotContain(KEY);
        });
    }

    @Test
    void anUnknownDriverOrAgreementStaysNotFound() {
        status = 404;
        body = "{\"code\":\"RESOURCE_NOT_FOUND\"}";
        assertThatThrownBy(() -> client().revoke("root", agreement)).isInstanceOfSatisfying(ApiException.class, e -> {
            assertThat(e.status().value()).isEqualTo(404);
            assertThat(e.code()).isEqualTo("RESOURCE_NOT_FOUND");
        });
    }

    @Test
    void aNotFoundOnTheListCreateOrUsageRoutesIsUnavailable() {
        status = 404;
        body = "{\"code\":\"RESOURCE_NOT_FOUND\"}";
        assertThatThrownBy(() -> client().list("root")).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.code()).isEqualTo("FREE_CHARGING_UNAVAILABLE"));
        assertThatThrownBy(() -> client().usage("root", 0, 20)).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.status().value()).isEqualTo(503));
        assertThatThrownBy(() -> client().create("root", driver,
                new AgreementTerms(LocalDate.of(2026, 11, 1), null, null))).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.code()).isEqualTo("FREE_CHARGING_UNAVAILABLE"));
    }

    @Test
    void aServerErrorBecomesUnavailable() {
        assertUnavailable(500, "{\"code\":\"BOOM\",\"trace\":\"secret internals\"}");
    }

    @Test
    void aRefusedKeyBecomesUnavailableNotAPassThrough() {
        assertUnavailable(401, "{\"code\":\"INVALID_CREDENTIALS\"}");
    }

    @Test
    void aRedirectBecomesUnavailable() {
        assertUnavailable(302, "");
    }

    @Test
    void anUnreadableAnswerBecomesUnavailable() {
        assertUnavailable(200, "<html>not json</html>");
    }

    @Test
    void aClientErrorWithoutAStableCodeBecomesUnavailable() {
        assertUnavailable(422, "garbage");
    }

    @Test
    void aSlowNdzelaBecomesUnavailable() {
        delayMillis = 1500;
        body = "[]";
        assertThatThrownBy(() -> client(Duration.ofMillis(200)).list("root"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("FREE_CHARGING_UNAVAILABLE"));
    }

    @Test
    void anUnconfiguredClientIsUnavailable() {
        HttpFreeChargingAdministration unconfigured = new HttpFreeChargingAdministration(
                new NdzelaRestClient(new NdzelaSettings("", ""), Duration.ofSeconds(1), Duration.ofSeconds(1)));
        assertThatThrownBy(() -> unconfigured.list("root")).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.status().value()).isEqualTo(503));
    }

    private void assertUnavailable(int answerStatus, String answerBody) {
        status = answerStatus;
        body = answerBody;
        assertThatThrownBy(() -> client().suspend("root", agreement)).isInstanceOfSatisfying(ApiException.class, e -> {
            assertThat(e.status().value()).isEqualTo(503);
            assertThat(e.code()).isEqualTo("FREE_CHARGING_UNAVAILABLE");
            assertThat(e.getMessage()).doesNotContain(KEY).doesNotContain("secret internals");
        });
    }
}

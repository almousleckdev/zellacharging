package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminFreeChargingIntegrationTest extends PostgresTestBase {

    private static final String KEY = "shared-partner-key-123";
    private static final String BASE = "/api/v1/partners/zella/free-charging";
    private static final AtomicReference<String> METHOD = new AtomicReference<>();
    private static final AtomicReference<String> TARGET = new AtomicReference<>();
    private static final AtomicReference<String> SEEN_KEY = new AtomicReference<>();
    private static final AtomicReference<String> SEEN_ACTOR = new AtomicReference<>();
    private static final AtomicReference<String> SENT = new AtomicReference<>();
    private static final AtomicInteger CALLS = new AtomicInteger();
    private static volatile int answerStatus = 200;
    private static volatile String answerBody = "[]";
    private static final HttpServer NDZELA = startNdzela();

    private static HttpServer startNdzela() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                CALLS.incrementAndGet();
                METHOD.set(exchange.getRequestMethod());
                TARGET.set(exchange.getRequestURI().toString());
                SEEN_KEY.set(exchange.getRequestHeaders().getFirst("X-Api-Key"));
                SEEN_ACTOR.set(exchange.getRequestHeaders().getFirst("X-Actor"));
                SENT.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] bytes = answerBody.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(answerStatus, bytes.length == 0 ? -1 : bytes.length);
                if (bytes.length > 0) {
                    exchange.getResponseBody().write(bytes);
                }
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void ndzela(DynamicPropertyRegistry registry) {
        registry.add("zella.ndzela.base-url", () -> "http://127.0.0.1:" + NDZELA.getAddress().getPort());
        registry.add("zella.ndzela.api-key", () -> KEY);
    }

    @AfterAll
    static void stopNdzela() {
        NDZELA.stop(0);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @Autowired
    JdbcTemplate jdbc;

    private AdminFixtures fixtures;
    private AdminFixtures.Created manager;
    private AdminFixtures.Created root;
    private final UUID agreement = UUID.randomUUID();
    private final UUID driver = UUID.randomUUID();

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        manager = fixtures.create(AdminRole.ADMIN, Set.of("GA"));
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());
        answerStatus = 200;
        answerBody = agreementJson();
        CALLS.set(0);
        METHOD.set(null);
        TARGET.set(null);
        SEEN_KEY.set(null);
        SEEN_ACTOR.set(null);
    }

    private String agreementJson() {
        return "{\"id\":\"" + agreement + "\",\"driverId\":\"" + driver + "\",\"driverName\":\"Ada Obiang\","
                + "\"startsOn\":\"2026-11-01\",\"endsOn\":null,\"status\":\"ACTIVE\",\"conditions\":null,"
                + "\"eligibility\":\"ELIGIBLE\",\"unpaidDays\":0,\"lastFreeUse\":null,\"phone\":\"+24177001122\","
                + "\"email\":\"ada@x.test\",\"fleetNumber\":\"V015\",\"plate\":\"PL-0150\"}";
    }

    private ResultActions call(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private ResultActions call(MockHttpServletRequestBuilder request) throws Exception {
        return call(request, manager.accessToken());
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private String create() {
        return createFor(linkedCustomer("GA"));
    }

    private String createFor(UUID customer) {
        return "{\"customerId\":\"" + customer + "\",\"startsOn\":\"2026-11-01\",\"conditions\":\"none\"}";
    }

    private UUID linkedCustomer(String country) {
        UUID customer = newCustomer(country);
        try {
            mvc.perform(put("/v1/admin/customers/" + country + "/" + customer + "/ndzela-link")
                    .header("Authorization", "Bearer " + root.accessToken())).andExpect(status().isNoContent());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return customer;
    }

    private UUID newCustomer(String country) {
        try {
            String json = mvc.perform(post("/v1/admin/customers/" + country).header("Authorization", "Bearer " + root.accessToken())
                    .contentType(MediaType.APPLICATION_JSON).content("{\"firstName\":\"Ada\",\"lastName\":\"Obiang\",\"email\":\"ada."
                            + UUID.randomUUID() + "@example.com\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            return UUID.fromString(json.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String terms() {
        return "{\"startsOn\":\"2026-11-01\",\"endsOn\":\"2027-01-01\"}";
    }

    private int audited(String action) {
        return jdbc.queryForObject("select count(*) from admin_audit_record where action = ? and admin_id = ?",
                Integer.class, action, manager.id());
    }

    private int auditedAny() {
        return jdbc.queryForObject("select count(*) from admin_audit_record where action like 'FREE_AGREEMENT_%' "
                + "and admin_id = ?", Integer.class, manager.id());
    }

    @Test
    void anAdminListsAgreementsAndNdzelaGetsTheKeyAndTheActor() throws Exception {
        answerBody = "[" + agreementJson() + "]";
        call(get("/v1/admin/free-charging/agreements")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(agreement.toString()))
                .andExpect(jsonPath("$[0].driverName").value("Ada Obiang"))
                .andExpect(jsonPath("$[0].eligibility").value("ELIGIBLE"))
                .andExpect(jsonPath("$[0].unpaidDays").value(0))
                .andExpect(jsonPath("$[0].phone").value("+24177001122")).andExpect(jsonPath("$[0].fleetNumber").value("V015"))
                .andExpect(jsonPath("$[0].plate").value("PL-0150")).andExpect(jsonPath("$[0].email").value("ada@x.test"));
        assertThat(TARGET.get()).isEqualTo(BASE + "/agreements");
        assertThat(SEEN_KEY.get()).isEqualTo(KEY);
        assertThat(SEEN_ACTOR.get()).isEqualTo(manager.email());
    }

    @Test
    void usageIsListedAndPaged() throws Exception {
        answerBody = "{\"items\":[],\"page\":2,\"size\":5,\"total\":0}";
        call(get("/v1/admin/free-charging/usage?page=2&size=5")).andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2)).andExpect(jsonPath("$.total").value(0));
        assertThat(TARGET.get()).isEqualTo(BASE + "/usage?page=2&size=5");
        assertThat(SEEN_ACTOR.get()).isEqualTo(manager.email());
    }

    @Test
    void anOversizedPageIsRefusedBeforeNdzelaIsCalled() throws Exception {
        call(get("/v1/admin/free-charging/usage?size=1000")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGING"));
        assertThat(CALLS.get()).isZero();
    }

    @Test
    void everyMutationForwardsTheActorAndIsAuditedOnce() throws Exception {
        call(json(post("/v1/admin/free-charging/agreements"), create())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(agreement.toString()));
        assertThat(METHOD.get()).isEqualTo("POST");
        assertThat(SEEN_ACTOR.get()).isEqualTo(manager.email());
        assertThat(audited("FREE_AGREEMENT_CREATED")).isEqualTo(1);

        call(json(put("/v1/admin/free-charging/agreements/" + agreement), terms())).andExpect(status().isOk());
        assertThat(METHOD.get()).isEqualTo("PUT");
        assertThat(audited("FREE_AGREEMENT_UPDATED")).isEqualTo(1);

        call(post("/v1/admin/free-charging/agreements/" + agreement + "/suspend")).andExpect(status().isOk());
        assertThat(TARGET.get()).isEqualTo(BASE + "/agreements/" + agreement + "/suspend");
        assertThat(audited("FREE_AGREEMENT_SUSPENDED")).isEqualTo(1);

        call(post("/v1/admin/free-charging/agreements/" + agreement + "/resume")).andExpect(status().isOk());
        assertThat(audited("FREE_AGREEMENT_RESUMED")).isEqualTo(1);

        call(post("/v1/admin/free-charging/agreements/" + agreement + "/revoke")).andExpect(status().isOk());
        assertThat(audited("FREE_AGREEMENT_REVOKED")).isEqualTo(1);
        assertThat(auditedAny()).isEqualTo(5);
        assertThat(SEEN_KEY.get()).isEqualTo(KEY);
    }

    @Test
    void theCreateForwardsTheCustomerIdAndTheAuditDetailNamesTheCustomer() throws Exception {
        UUID customer = linkedCustomer("GA");
        call(json(post("/v1/admin/free-charging/agreements"), createFor(customer))).andExpect(status().isCreated());
        assertThat(SENT.get()).contains("\"customerId\":\"" + customer + "\"").doesNotContain("driverId");
        assertThat(jdbc.queryForObject("select detail from admin_audit_record where action = 'FREE_AGREEMENT_CREATED' and admin_id = ?",
                String.class, manager.id())).isEqualTo("customer " + customer);
    }

    @Test
    void aCustomerNotLinkedToNdzelaIsRefusedWithoutCallingNdzela() throws Exception {
        call(json(post("/v1/admin/free-charging/agreements"), createFor(newCustomer("GA"))))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("FREE_AGREEMENT_NOT_LINKED"));
        call(json(post("/v1/admin/free-charging/agreements"), createFor(UUID.randomUUID())))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("FREE_AGREEMENT_NOT_LINKED"));
        assertThat(CALLS.get()).isZero();
        assertThat(auditedAny()).isZero();
    }

    @Test
    void aLinkedCustomerOfAnotherCountryIsNotLinkedHere() throws Exception {
        call(json(post("/v1/admin/free-charging/agreements"), createFor(linkedCustomer("ML"))))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("FREE_AGREEMENT_NOT_LINKED"));
        assertThat(CALLS.get()).isZero();
    }

    @Test
    void anNdzelaRefusalIsPassedThroughAndNotAudited() throws Exception {
        answerStatus = 422;
        answerBody = "{\"code\":\"FREE_AGREEMENT_EXISTS\",\"message\":\"x\"}";
        call(json(post("/v1/admin/free-charging/agreements"), create())).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("FREE_AGREEMENT_EXISTS"));
        assertThat(auditedAny()).isZero();
    }

    @Test
    void anNdzelaOutageIsUnavailableWithoutAnAuditEntryAndWithoutTheKey() throws Exception {
        answerStatus = 500;
        answerBody = "{\"trace\":\"" + KEY + "\"}";
        call(post("/v1/admin/free-charging/agreements/" + agreement + "/revoke"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("FREE_CHARGING_UNAVAILABLE"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(KEY))));
        assertThat(auditedAny()).isZero();
    }

    @Test
    void aMalformedAgreementIdIsABadRequestAndNdzelaIsNotCalled() throws Exception {
        call(post("/v1/admin/free-charging/agreements/not-a-uuid/suspend")).andExpect(status().isBadRequest());
        call(json(put("/v1/admin/free-charging/agreements/123"), terms())).andExpect(status().isBadRequest());
        call(json(post("/v1/admin/free-charging/agreements"),
                "{\"customerId\":\"nope\",\"startsOn\":\"2026-11-01\"}")).andExpect(status().isBadRequest());
        assertThat(CALLS.get()).isZero();
    }

    @Test
    void aReadOnlyRoleListsButCannotMutate() throws Exception {
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();
        answerBody = "[]";
        call(get("/v1/admin/free-charging/agreements"), support).andExpect(status().isOk());
        answerBody = "{\"items\":[],\"page\":0,\"size\":20,\"total\":0}";
        call(get("/v1/admin/free-charging/usage"), support).andExpect(status().isOk());
        call(json(post("/v1/admin/free-charging/agreements"), create()), support).andExpect(status().isForbidden());
        call(json(put("/v1/admin/free-charging/agreements/" + agreement), terms()), support)
                .andExpect(status().isForbidden());
        for (String action : new String[]{"suspend", "resume", "revoke"}) {
            call(post("/v1/admin/free-charging/agreements/" + agreement + "/" + action), support)
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
        }
        assertThat(CALLS.get()).isEqualTo(2);
    }

    @Test
    void anAdminWithoutAnyFreeChargingPermissionIsRefusedEverywhere() throws Exception {
        String bare = fixtures.createWith(AdminRole.ADMIN, Set.of("GA"), Set.of(Permission.OVERVIEW_READ)).accessToken();
        call(get("/v1/admin/free-charging/agreements"), bare).andExpect(status().isForbidden());
        call(get("/v1/admin/free-charging/usage"), bare).andExpect(status().isForbidden());
        call(json(post("/v1/admin/free-charging/agreements"), create()), bare).andExpect(status().isForbidden());
        call(post("/v1/admin/free-charging/agreements/" + agreement + "/revoke"), bare)
                .andExpect(status().isForbidden());
        assertThat(CALLS.get()).isZero();
    }

    @Test
    void theActorSentToNdzelaIsSanitizedAndCapped() throws Exception {
        answerBody = "[]";
        String longName = "x".repeat(300);
        AdminFixtures.Created odd = fixtures.createWith(AdminRole.ADMIN, Set.of("GA"),
                Set.of(Permission.FREE_CHARGING_READ));
        jdbc.update("update admin_user set email = ? where id = ?", longName + "@zella.test", odd.id());
        call(get("/v1/admin/free-charging/agreements"), odd.accessToken()).andExpect(status().isOk());
        assertThat(SEEN_ACTOR.get()).hasSize(100);
    }

    @Test
    void anAdminWithoutAGrantOnTheNdzelaCountryIsRefusedOnEveryRouteAndNdzelaIsNotCalled() throws Exception {
        String abroad = fixtures.createWith(AdminRole.ADMIN, Set.of("ML"),
                Set.of(Permission.FREE_CHARGING_READ, Permission.FREE_CHARGING_MANAGE)).accessToken();
        String base = "/v1/admin/free-charging/agreements";
        call(get(base), abroad).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        call(get("/v1/admin/free-charging/usage"), abroad).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        call(json(post(base), create()), abroad).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        call(json(put(base + "/" + agreement), terms()), abroad).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        for (String action : new String[]{"suspend", "resume", "revoke"}) {
            call(post(base + "/" + agreement + "/" + action), abroad).andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        }
        assertThat(CALLS.get()).isZero();
        assertThat(jdbc.queryForObject("select count(*) from admin_audit_record where action like 'FREE_AGREEMENT_%'",
                Integer.class)).isZero();
    }

    @Test
    void theAuditEntryCarriesTheNdzelaCountry() throws Exception {
        call(post("/v1/admin/free-charging/agreements/" + agreement + "/suspend")).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("select country_code from admin_audit_record where action = ? and admin_id = ?",
                String.class, "FREE_AGREEMENT_SUSPENDED", manager.id())).isEqualTo("GA");
    }

    private static final String WINDOW_JSON = "{\"day\":\"SUNDAY\",\"from\":\"00:00\",\"until\":\"23:59\",\"endOfDay\":true}";

    private int windowAudits() {
        return jdbc.queryForObject("select count(*) from admin_audit_record where action = 'FREE_WINDOW_CHANGED' "
                + "and admin_id = ?", Integer.class, manager.id());
    }

    @Test
    void anAdminReadsTheWindowThroughNdzela() throws Exception {
        answerBody = WINDOW_JSON;
        call(get("/v1/admin/free-charging/window")).andExpect(status().isOk())
                .andExpect(jsonPath("$.day").value("SUNDAY")).andExpect(jsonPath("$.until").value("23:59"))
                .andExpect(jsonPath("$.endOfDay").value(true));
        assertThat(METHOD.get()).isEqualTo("GET");
        assertThat(TARGET.get()).isEqualTo(BASE + "/window");
        assertThat(SEEN_KEY.get()).isEqualTo(KEY);
        assertThat(SEEN_ACTOR.get()).isEqualTo(manager.email());
    }

    @Test
    void changingTheWindowIsForwardedWithTheActorAndAuditedOnce() throws Exception {
        answerBody = "{\"day\":\"SATURDAY\",\"from\":\"09:00\",\"until\":\"17:30\",\"endOfDay\":false}";
        call(json(put("/v1/admin/free-charging/window"), "{\"day\":\"SATURDAY\",\"from\":\"09:00\",\"until\":\"17:30\","
                + "\"endOfDay\":false}")).andExpect(status().isOk()).andExpect(jsonPath("$.day").value("SATURDAY"));
        assertThat(METHOD.get()).isEqualTo("PUT");
        assertThat(TARGET.get()).isEqualTo(BASE + "/window");
        assertThat(SENT.get()).contains("\"day\":\"SATURDAY\"").contains("\"from\":\"09:00\"").contains("\"until\":\"17:30\"");
        assertThat(windowAudits()).isEqualTo(1);
    }

    @Test
    void aWindowRefusedByNdzelaIsPassedThroughAndNotAudited() throws Exception {
        answerStatus = 422;
        answerBody = "{\"code\":\"FREE_WINDOW_INVALID\",\"message\":\"x\"}";
        call(json(put("/v1/admin/free-charging/window"), WINDOW_JSON)).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("FREE_WINDOW_INVALID"));
        assertThat(windowAudits()).isZero();
    }

    @Test
    void aBadWindowBodyIsABadRequestAndNdzelaIsNotCalled() throws Exception {
        call(json(put("/v1/admin/free-charging/window"), "{\"from\":\"00:00\",\"endOfDay\":true}")).andExpect(status().isBadRequest());
        call(json(put("/v1/admin/free-charging/window"), "{\"day\":\"FUNDAY\",\"from\":\"00:00\",\"endOfDay\":true}")).andExpect(status().isBadRequest());
        call(json(put("/v1/admin/free-charging/window"), "{\"day\":\"SUNDAY\",\"from\":\"25:99\",\"endOfDay\":true}")).andExpect(status().isBadRequest());
        assertThat(CALLS.get()).isZero();
    }

    @Test
    void aReadOnlyRoleReadsTheWindowButCannotChangeIt() throws Exception {
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();
        answerBody = WINDOW_JSON;
        call(get("/v1/admin/free-charging/window"), support).andExpect(status().isOk());
        call(json(put("/v1/admin/free-charging/window"), WINDOW_JSON), support).andExpect(status().isForbidden());
        assertThat(windowAudits()).isZero();
    }

    @Test
    void anAdminOfAnotherCountryCannotReadOrChangeTheWindow() throws Exception {
        String abroad = fixtures.createWith(AdminRole.ADMIN, Set.of("ML"),
                Set.of(Permission.FREE_CHARGING_READ, Permission.FREE_CHARGING_MANAGE)).accessToken();
        call(get("/v1/admin/free-charging/window"), abroad).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        call(json(put("/v1/admin/free-charging/window"), WINDOW_JSON), abroad).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        assertThat(CALLS.get()).isZero();
    }

    private static final String DRIVER_JSON = "{\"items\":[{\"driverId\":\"" + UUID.randomUUID() + "\",\"customerId\":\"" + UUID.randomUUID()
            + "\",\"fullName\":\"NZE Paul\",\"phone\":\"+24177001122\",\"email\":\"paul@x.test\",\"fleetNumber\":\"V015\","
            + "\"plate\":\"PL-0150\",\"status\":\"ACTIVE\",\"linked\":true,\"inProgram\":true,\"agreementId\":\"" + UUID.randomUUID()
            + "\",\"agreementStatus\":\"SUSPENDED\"}],\"page\":2,\"size\":10,\"total\":31}";

    @Test
    void anAdminSearchesNdzelaDriversAndTheTextIsPassedEncodedAndBounded() throws Exception {
        answerBody = DRIVER_JSON;
        call(get("/v1/admin/free-charging/drivers").param("q", "V015 & {x}+1").param("size", "10").param("page", "2").param("membership", "IN_PROGRAM")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].fullName").value("NZE Paul")).andExpect(jsonPath("$.items[0].fleetNumber").value("V015"))
                .andExpect(jsonPath("$.items[0].linked").value(true)).andExpect(jsonPath("$.items[0].inProgram").value(true))
                .andExpect(jsonPath("$.items[0].agreementStatus").value("SUSPENDED")).andExpect(jsonPath("$.total").value(31)).andExpect(jsonPath("$.page").value(2));
        assertThat(METHOD.get()).isEqualTo("GET");
        assertThat(TARGET.get()).startsWith(BASE + "/drivers?q=").contains("&membership=IN_PROGRAM").contains("&page=2").endsWith("&size=10");
        assertThat(TARGET.get()).doesNotContain(" ").doesNotContain("{").doesNotContain("&x").contains("%26");
        assertThat(SEEN_ACTOR.get()).isEqualTo(manager.email());
    }

    @Test
    void theSearchTextAndSizeAreBounded() throws Exception {
        answerBody = "{\"items\":[],\"page\":0,\"size\":1,\"total\":0}";
        call(get("/v1/admin/free-charging/drivers").param("q", "a".repeat(300)).param("size", "100000").param("page", "-5").param("membership", "evil")).andExpect(status().isOk());
        assertThat(TARGET.get()).contains("size=100").contains("page=0").contains("membership=ALL");
        assertThat(TARGET.get().length()).isLessThan(BASE.length() + 200);
        call(get("/v1/admin/free-charging/drivers").param("size", "-4")).andExpect(status().isOk());
        assertThat(TARGET.get()).contains("size=1");
    }

    @Test
    void searchingDriversNeedsTheManagePermissionAndTheCountry() throws Exception {
        String reader = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();
        call(get("/v1/admin/free-charging/drivers"), reader).andExpect(status().isForbidden());
        String abroad = fixtures.createWith(AdminRole.ADMIN, Set.of("ML"),
                Set.of(Permission.FREE_CHARGING_READ, Permission.FREE_CHARGING_MANAGE)).accessToken();
        call(get("/v1/admin/free-charging/drivers"), abroad).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
        assertThat(CALLS.get()).isZero();
    }

    @Test
    void anNdzelaOutageDuringTheSearchIsUnavailable() throws Exception {
        answerStatus = 500;
        call(get("/v1/admin/free-charging/drivers").param("q", "x")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("FREE_CHARGING_UNAVAILABLE"));
    }
}

package com.evcharge.customer.charging.application;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.partner.application.port.NdzelaFreeCharging;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.UUID;

import static com.evcharge.customer.charging.application.FreeSundayFixtures.eligible;
import static com.evcharge.customer.charging.application.FreeSundayFixtures.finished;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
abstract class FreeSundayTestBase extends PostgresTestBase {

    static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[]}");

    @DynamicPropertySource
    static void ocpp(DynamicPropertyRegistry registry) {
        registry.add("zella.ocpp.base-url", OCPP::baseUrl);
    }

    @Autowired
    SessionService service;

    @Autowired
    SessionSettlement settlement;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    NdzelaFreeCharging ndzela;

    @MockitoSpyBean
    SessionRepository sessions;

    FreeSundayFixtures fixtures;

    @BeforeEach
    void ndzelaIsUnavailableUnlessTold() {
        fixtures = new FreeSundayFixtures(jdbc, OCPP);
        when(ndzela.check(any())).thenReturn(FreeChargingEligibility.unavailable());
    }

    UUID driver(long balance) {
        UUID customer = fixtures.linkedCustomerWith(balance);
        when(ndzela.check(customer)).thenReturn(eligible());
        return customer;
    }

    ChargingSession start(UUID customer, String station, String key) {
        return service.start(customer, station, 1, 2000, key);
    }

    void close(ChargingSession session, SessionState state, BigDecimal kwh) {
        ChargingSession ended = finished(session, state, kwh, 1500);
        sessions.update(ended);
        settlement.settle(ended);
    }
}

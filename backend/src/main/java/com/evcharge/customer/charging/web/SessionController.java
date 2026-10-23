package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionService;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/sessions")
class SessionController {

    private final SessionService sessions;
    private final MeteringService metering;
    private final SessionCostEstimator estimator;
    private final FeeCalculator calculator;

    SessionController(SessionService sessions, MeteringService metering, SessionCostEstimator estimator,
                      FeeCalculator calculator) {
        this.sessions = sessions;
        this.metering = metering;
        this.estimator = estimator;
        this.calculator = calculator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SessionResponse start(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody StartSessionRequest request) {
        return present(sessions.start(customer(jwt), request.stationId(), request.connectorId(),
                request.amountMinor()));
    }

    @GetMapping("/active")
    ResponseEntity<SessionResponse> active(@AuthenticationPrincipal Jwt jwt) {
        return sessions.active(customer(jwt)).map(session -> ResponseEntity.ok(present(session)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    List<SessionResponse> history(@AuthenticationPrincipal Jwt jwt) {
        return sessions.history(customer(jwt)).stream().map(session -> SessionResponse.from(session, null, LastReading.empty(), Optional.empty(), fees(session))).toList();
    }

    @GetMapping("/{id}")
    SessionResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return present(sessions.get(customer(jwt), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        sessions.hide(customer(jwt), id);
    }

    @PostMapping("/{id}/stop")
    SessionResponse stop(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return present(sessions.stop(customer(jwt), id));
    }

    private Optional<FeeBreakdown> fees(ChargingSession session) {
        return Optional.ofNullable(session.costMinor()).map(calculator::of);
    }

    private SessionResponse present(ChargingSession session) {
        Optional<Metering> reading = metering.of(session);
        return SessionResponse.from(session, reading.orElse(null), metering.lastReading(session, reading),
                estimator.estimate(session, reading), fees(session));
    }

    private static UUID customer(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}

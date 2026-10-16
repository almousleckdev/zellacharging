package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.SessionService;
import com.evcharge.customer.charging.domain.ChargingSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/sessions")
class SessionController {

    private final SessionService sessions;
    private final MeteringService metering;

    SessionController(SessionService sessions, MeteringService metering) {
        this.sessions = sessions;
        this.metering = metering;
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
        return sessions.history(customer(jwt)).stream().map(session -> SessionResponse.from(session, null)).toList();
    }

    @GetMapping("/{id}")
    SessionResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return present(sessions.get(customer(jwt), id));
    }

    @PostMapping("/{id}/stop")
    SessionResponse stop(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return present(sessions.stop(customer(jwt), id));
    }

    private SessionResponse present(ChargingSession session) {
        return SessionResponse.from(session, metering.of(session).orElse(null));
    }

    private static UUID customer(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}

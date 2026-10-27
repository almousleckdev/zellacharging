package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.application.SessionQuoteService;
import com.evcharge.customer.charging.application.SessionService;
import com.evcharge.customer.shared.CustomerPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/sessions")
class SessionController {

    private final SessionService sessions;
    private final SessionQuoteService quotes;
    private final SessionPresenter presenter;

    SessionController(SessionService sessions, SessionQuoteService quotes, SessionPresenter presenter) {
        this.sessions = sessions;
        this.quotes = quotes;
        this.presenter = presenter;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SessionResponse start(CustomerPrincipal me, @Valid @RequestBody StartSessionRequest request,
                          @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        return presenter.live(sessions.start(me.id(), request.stationId(), request.connectorId(),
                request.amountMinor(), blankToNull(idempotencyKey)));
    }

    @PostMapping("/quote")
    SessionQuoteResponse quote(CustomerPrincipal me, @Valid @RequestBody StartSessionRequest request) {
        return SessionQuoteResponse.from(quotes.quote(me.id(), request.stationId(), request.connectorId(),
                request.amountMinor()));
    }

    @GetMapping("/active")
    ResponseEntity<SessionResponse> active(CustomerPrincipal me) {
        return sessions.active(me.id()).map(session -> ResponseEntity.ok(presenter.live(session)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    List<SessionResponse> history(CustomerPrincipal me) {
        return presenter.summaries(sessions.history(me.id()));
    }

    @GetMapping("/{id}")
    SessionResponse get(CustomerPrincipal me, @PathVariable UUID id) {
        return presenter.live(sessions.get(me.id(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(CustomerPrincipal me, @PathVariable UUID id) {
        sessions.hide(me.id(), id);
    }

    @PostMapping("/{id}/stop")
    SessionResponse stop(CustomerPrincipal me, @PathVariable UUID id) {
        return presenter.live(sessions.stop(me.id(), id));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

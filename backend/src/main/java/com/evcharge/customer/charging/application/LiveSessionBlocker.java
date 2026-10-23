package com.evcharge.customer.charging.application;

import com.evcharge.customer.account.ClosureBlocker;
import com.evcharge.customer.charging.application.port.SessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class LiveSessionBlocker implements ClosureBlocker {

    private final SessionRepository sessions;

    LiveSessionBlocker(SessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public Optional<String> blockingCode(UUID customerId) {
        return sessions.findLive(customerId).map(session -> "SESSION_STILL_ACTIVE");
    }
}

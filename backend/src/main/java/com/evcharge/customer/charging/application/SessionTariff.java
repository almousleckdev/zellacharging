package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SessionTariff {

    private final SessionRepository sessions;
    private final SessionSettings settings;

    public SessionTariff(SessionRepository sessions, SessionSettings settings) {
        this.sessions = sessions;
        this.settings = settings;
    }

    public Optional<Long> pricePerKwhMinor(UUID sessionId) {
        return sessions.priceOf(sessionId).or(() -> Optional.ofNullable(settings.pricePerKwhMinor()));
    }
}

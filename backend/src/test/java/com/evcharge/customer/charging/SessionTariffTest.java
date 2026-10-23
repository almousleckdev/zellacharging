package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.SessionTariff;
import com.evcharge.customer.charging.application.port.SessionRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionTariffTest {

    private final SessionRepository sessions = mock(SessionRepository.class);
    private final UUID session = UUID.randomUUID();

    private SessionTariff tariff(Long configured) {
        return new SessionTariff(sessions, new SessionSettings(Duration.ofSeconds(60), Duration.ofSeconds(15),
                Duration.ofSeconds(30), configured, 95));
    }

    @Test
    void usesThePriceTheChargerPlatformSentForTheSession() {
        when(sessions.priceOf(session)).thenReturn(Optional.of(350L));

        assertThat(tariff(150L).pricePerKwhMinor(session)).contains(350L);
    }

    @Test
    void fallsBackToTheConfiguredPrice() {
        when(sessions.priceOf(session)).thenReturn(Optional.empty());

        assertThat(tariff(150L).pricePerKwhMinor(session)).contains(150L);
    }

    @Test
    void hasNoPriceWhenNobodyKnowsIt() {
        when(sessions.priceOf(session)).thenReturn(Optional.empty());

        assertThat(tariff(null).pricePerKwhMinor(session)).isEmpty();
    }
}

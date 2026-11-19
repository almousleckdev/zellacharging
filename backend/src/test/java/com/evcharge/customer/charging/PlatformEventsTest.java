package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.PlatformEvents;
import com.evcharge.customer.charging.application.SessionReconciler;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.PlatformEvent;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformEventsTest {

    private static final Instant NOW = Instant.parse("2026-11-05T18:00:00Z");

    private final SessionRepository sessions = mock(SessionRepository.class);
    private final SessionTelemetry telemetry = mock(SessionTelemetry.class);
    private final SessionReconciler reconciler = mock(SessionReconciler.class);
    private final CountryDirectory countries = mock(CountryDirectory.class);
    private final PlatformEvents events = new PlatformEvents(sessions, telemetry, reconciler, countries, Runnable::run);

    private final ChargingSession live = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-9", 1, "Z-A",
            SessionState.ACTIVE, null, 5, null, null, null, null, null, null, null, NOW.minusSeconds(60), NOW);

    private static PlatformEvent event(String id, String type, Metering metering) {
        return new PlatformEvent(id, type, "CP-9", 1, 5, "Z-A", metering);
    }

    private void aLiveSession() {
        when(countries.active()).thenReturn(List.of(new Country("GA", "Gabon", "XAF", 0, "GA", "Africa/Libreville")));
        when(sessions.findLiveAt("CP-9", 1, "Z-A")).thenReturn(Optional.of(live));
    }

    @Test
    void aMeteringEventFeedsTheLiveReadingOfItsSession() {
        aLiveSession();
        Metering reading = new Metering(new BigDecimal("4.2"), new BigDecimal("30"), new BigDecimal("93"), 600L,
                new BigDecimal("400"), new BigDecimal("75"), NOW);

        events.accept(event("e1", "session.metering", reading));

        verify(telemetry).recordReading(live.id(), reading);
    }

    @Test
    void aSessionThatIsNotLiveHereIsIgnored() {
        when(countries.active()).thenReturn(List.of(new Country("GA", "Gabon", "XAF", 0, "GA", "Africa/Libreville")));
        when(sessions.findLiveAt("CP-9", 1, "Z-A")).thenReturn(Optional.empty());

        events.accept(event("e1", "session.metering", new Metering(null, null, null, null, null, null, NOW)));

        verify(telemetry, never()).recordReading(any(), any());
    }

    @Test
    void theSameEventIsHandledOnlyOnce() {
        aLiveSession();
        Metering reading = new Metering(new BigDecimal("1"), null, null, null, null, null, NOW);

        events.accept(event("e1", "session.metering", reading));
        events.accept(event("e1", "session.metering", reading));

        verify(telemetry, times(1)).recordReading(live.id(), reading);
    }

    @Test
    void startedAndStoppedEventsRefreshTheSessionFromThePlatform() {
        aLiveSession();

        events.accept(event("e1", "session.started", null));
        events.accept(event("e2", "session.stopped", null));

        verify(reconciler, times(2)).reconcile(live);
    }

    @Test
    void unknownEventTypesAreIgnored() {
        aLiveSession();

        events.accept(event("e1", "device.online", null));

        verify(reconciler, never()).reconcile(any());
        verify(telemetry, never()).recordReading(any(), any());
    }

    @Test
    void eachCountryIsSearchedUnderItsOwnTenant() {
        when(countries.active()).thenReturn(List.of(new Country("GA", "Gabon", "XAF", 0, "GA", "Africa/Libreville"),
                new Country("ML", "Mali", "XOF", 0, "ML", "Africa/Bamako")));
        AtomicReference<String> foundUnder = new AtomicReference<>();
        when(sessions.findLiveAt("CP-9", 1, "Z-A")).thenAnswer(call -> {
            String tenant = TenantContext.current().orElseThrow();
            if (tenant.equals("ML")) {
                foundUnder.set(tenant);
                return Optional.of(live);
            }
            return Optional.empty();
        });

        events.accept(event("e1", "session.started", null));

        assertThat(foundUnder.get()).isEqualTo("ML");
        verify(reconciler).reconcile(live);
    }
}

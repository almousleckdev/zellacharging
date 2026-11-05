package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionProgress;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeteringService {

    private static final Duration PUSHED_READING_FRESHNESS = Duration.ofSeconds(120);

    private record Fetched(Instant at, Optional<Metering> reading) {
    }

    private final ChargingGateway gateway;
    private final SessionTelemetry telemetry;
    private final SessionSettings settings;
    private final Clock clock;
    private final Map<UUID, Fetched> fetched = new ConcurrentHashMap<>();

    public MeteringService(ChargingGateway gateway, SessionTelemetry telemetry, SessionSettings settings,
                           Clock clock) {
        this.gateway = gateway;
        this.telemetry = telemetry;
        this.settings = settings;
        this.clock = clock;
    }

    public Optional<Metering> of(ChargingSession session) {
        if (!session.state().isLive() || session.transactionId() == null) {
            fetched.remove(session.id());
            return Optional.empty();
        }
        Fetched last = fetched.get(session.id());
        Instant now = clock.instant();
        if (last != null && last.at().plus(settings.meteringInterval()).isAfter(now)) {
            return last.reading();
        }
        try {
            Optional<Metering> polled = gateway.metering(session.transactionId());
            polled.ifPresent(found -> telemetry.recordReading(session.id(), found));
            Optional<Metering> reading = polled.isPresent() ? polled : pushed(session, now);
            fetched.put(session.id(), new Fetched(now, reading));
            return reading;
        } catch (ApiException e) {
            if (e.status() == HttpStatus.SERVICE_UNAVAILABLE && last != null) {
                return last.reading();
            }
            throw e;
        }
    }

    private Optional<Metering> pushed(ChargingSession session, Instant now) {
        SessionProgress progress = telemetry.progressOf(session.id());
        boolean fresh = progress.at() != null && progress.energyKwh() != null
                && progress.at().plus(PUSHED_READING_FRESHNESS).isAfter(now);
        if (!fresh) {
            return Optional.empty();
        }
        LastReading last = telemetry.lastReadingOf(session.id());
        return Optional.of(new Metering(progress.energyKwh(), last.powerKw(), last.batteryPercent(), null,
                last.voltage(), null, progress.at()));
    }

    public LastReading lastReading(ChargingSession session, Optional<Metering> reading) {
        LastReading remembered = telemetry.lastReadingOf(session.id());
        return reading.map(LastReading::of).map(live -> live.orElse(remembered)).orElse(remembered);
    }
}

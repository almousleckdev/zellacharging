package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.MeteringProjection;
import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.SessionTariff;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
class SessionPresenter {

    private final MeteringService metering;
    private final MeteringProjection projection;
    private final SessionCostEstimator estimator;
    private final FeeCalculator calculator;
    private final SessionTariff tariff;
    private final SessionSettings settings;

    SessionPresenter(MeteringService metering, MeteringProjection projection, SessionCostEstimator estimator,
                     FeeCalculator calculator, SessionTariff tariff, SessionSettings settings) {
        this.metering = metering;
        this.projection = projection;
        this.estimator = estimator;
        this.calculator = calculator;
        this.tariff = tariff;
        this.settings = settings;
    }

    SessionResponse live(ChargingSession session) {
        Optional<Metering> reading = projection.of(session, metering.of(session));
        return SessionResponse.from(session, reading.orElse(null), metering.lastReading(session, reading),
                estimator.estimate(session, reading), fees(session), price(session), waitingUntil(session));
    }

    List<SessionResponse> summaries(List<ChargingSession> sessions) {
        return sessions.stream().map(this::summary).toList();
    }

    private SessionResponse summary(ChargingSession session) {
        return SessionResponse.from(session, null, metering.lastReading(session, Optional.empty()), Optional.empty(),
                fees(session), price(session), waitingUntil(session));
    }

    private Instant waitingUntil(ChargingSession session) {
        return session.state() == SessionState.START_ACCEPTED
                ? session.createdAt().plus(settings.plugInTimeout()) : null;
    }

    private Long price(ChargingSession session) {
        return tariff.pricePerKwhMinor(session.id()).orElse(null);
    }

    private Optional<FeeBreakdown> fees(ChargingSession session) {
        return Optional.ofNullable(session.costMinor()).map(calculator::of);
    }
}

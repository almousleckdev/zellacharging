package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionTariff;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.Metering;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class SessionPresenter {

    private final MeteringService metering;
    private final SessionCostEstimator estimator;
    private final FeeCalculator calculator;
    private final SessionTariff tariff;

    SessionPresenter(MeteringService metering, SessionCostEstimator estimator, FeeCalculator calculator,
                     SessionTariff tariff) {
        this.metering = metering;
        this.estimator = estimator;
        this.calculator = calculator;
        this.tariff = tariff;
    }

    SessionResponse live(ChargingSession session) {
        Optional<Metering> reading = metering.of(session);
        return SessionResponse.from(session, reading.orElse(null), metering.lastReading(session, reading),
                estimator.estimate(session, reading), fees(session), price(session));
    }

    List<SessionResponse> summaries(List<ChargingSession> sessions) {
        return sessions.stream().map(this::summary).toList();
    }

    private SessionResponse summary(ChargingSession session) {
        return SessionResponse.from(session, null, metering.lastReading(session, Optional.empty()), Optional.empty(),
                fees(session), price(session));
    }

    private Long price(ChargingSession session) {
        return tariff.pricePerKwhMinor(session.id()).orElse(null);
    }

    private Optional<FeeBreakdown> fees(ChargingSession session) {
        return Optional.ofNullable(session.costMinor()).map(calculator::of);
    }
}

package com.evcharge.customer.charging.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ChargingMetrics {

    private final Counter stopRefused;
    private final Counter stopWithoutTariff;
    private final Counter settlementReview;
    private final DistributionSummary overshoot;

    public ChargingMetrics(MeterRegistry registry) {
        this.stopRefused = registry.counter("zella.charging.stop.refused");
        this.stopWithoutTariff = registry.counter("zella.charging.stop.without_tariff");
        this.settlementReview = registry.counter("zella.charging.settlement.review");
        this.overshoot = registry.summary("zella.charging.overshoot.minor");
    }

    public void stopRefused() {
        stopRefused.increment();
    }

    public void stopWithoutTariff() {
        stopWithoutTariff.increment();
    }

    public void settlementReview() {
        settlementReview.increment();
    }

    public void overshoot(long minor) {
        overshoot.record(minor);
    }
}

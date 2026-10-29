package com.evcharge.customer.catalog.infrastructure;

import com.evcharge.customer.catalog.application.port.StationDirectory;
import com.evcharge.customer.shared.HealthProbe;
import com.evcharge.customer.shared.ProbeResult;
import org.springframework.stereotype.Component;

@Component
class OcppHealthProbe implements HealthProbe {

    private final StationDirectory stations;

    OcppHealthProbe(StationDirectory stations) {
        this.stations = stations;
    }

    @Override
    public String name() {
        return "ocpp";
    }

    @Override
    public ProbeResult check() {
        long started = System.nanoTime();
        try {
            int count = stations.list(null, null, null).size();
            return new ProbeResult(true, elapsed(started), String.valueOf(count));
        } catch (RuntimeException failure) {
            return new ProbeResult(false, elapsed(started), failure.getClass().getSimpleName());
        }
    }

    private static long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}

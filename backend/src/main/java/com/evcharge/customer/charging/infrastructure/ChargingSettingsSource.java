package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.shared.SettingsSource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
class ChargingSettingsSource implements SettingsSource {

    private final SessionSettings sessions;
    private final FeeSettings fees;

    ChargingSettingsSource(SessionSettings sessions, FeeSettings fees) {
        this.sessions = sessions;
        this.fees = fees;
    }

    @Override
    public String section() {
        return "charging";
    }

    @Override
    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("pricePerKwhMinor", sessions.pricePerKwhMinor());
        values.put("serviceFeePercent", fees.serviceFeePercent());
        values.put("vatPercent", fees.vatPercent());
        values.put("stopThresholdPercent", sessions.stopThresholdPercent());
        values.put("plugInTimeoutSeconds", sessions.plugInTimeout().toSeconds());
        values.put("meteringIntervalSeconds", sessions.meteringInterval().toSeconds());
        return values;
    }
}

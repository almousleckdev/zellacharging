package com.evcharge.customer.shared;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
class ReportingSettingsSource implements SettingsSource {

    private final ReportingWindow reporting;

    ReportingSettingsSource(ReportingWindow reporting) {
        this.reporting = reporting;
    }

    @Override
    public String section() {
        return "reporting";
    }

    @Override
    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("reportingStart", reporting.start() == null ? null : reporting.start().toString());
        return values;
    }
}

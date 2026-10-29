package com.evcharge.customer.shared;

import java.util.Map;

public interface SettingsSource {

    String section();

    Map<String, Object> values();
}

package com.evcharge.customer.admin.application;

import java.util.Map;

public record PlatformSettings(String section, Map<String, Object> values) {
}

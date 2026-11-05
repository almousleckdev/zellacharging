package com.evcharge.customer.charging.domain;

public record PlatformEvent(String eventId, String type, String deviceId, int connectorId, Integer transactionId,
                            String idTag, Metering metering) {
}

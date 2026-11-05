package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.PlatformEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.evcharge.customer.shared.ApiException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

@Component
class PlatformEventParser {

    private final ObjectMapper mapper;

    PlatformEventParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    PlatformEvent parse(byte[] body) {
        try {
            JsonNode root = mapper.readTree(body);
            String id = text(root, "eventId");
            String type = text(root, "eventType");
            if (id == null || type == null) {
                throw malformed();
            }
            JsonNode data = root.path("data");
            Metering metering = "session.metering".equals(type) ? metering(data) : null;
            return new PlatformEvent(id, type, text(data, "deviceId"), data.path("connectorId").asInt(0),
                    data.path("transactionId").isNumber() ? data.path("transactionId").asInt() : null,
                    text(data, "idTag"), metering);
        } catch (JacksonException e) {
            throw malformed();
        }
    }

    private static Metering metering(JsonNode data) {
        String sampled = text(data, "sampledAt");
        return new Metering(number(data, "totalKwh"), number(data, "currentPowerKw"),
                number(data, "stateOfChargePercent"),
                data.path("durationSeconds").isNumber() ? data.path("durationSeconds").asLong() : null,
                number(data, "voltage"), number(data, "currentAmpere"),
                sampled == null ? Instant.now() : Instant.parse(sampled));
    }

    private static BigDecimal number(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isString() && !value.asString().isBlank() ? value.asString() : null;
    }

    private static ApiException malformed() {
        return new ApiException(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The event could not be read");
    }
}

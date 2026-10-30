package com.evcharge.customer.notification.infrastructure;

import com.evcharge.customer.notification.application.port.PushSender;
import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.notification.domain.PushResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
class FcmPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(FcmPushSender.class);

    private final FcmSettings settings;
    private final RestClient client = RestClient.create();
    private final FcmAccessToken accessToken;
    private final String sendUrl;

    FcmPushSender(FcmSettings settings, ObjectMapper mapper, Clock clock) {
        this.settings = settings;
        if (settings.configured()) {
            var account = mapper.readTree(settings.credentialsJson());
            this.accessToken = new FcmAccessToken(account, client, mapper, clock);
            this.sendUrl = "https://fcm.googleapis.com/v1/projects/%s/messages:send"
                    .formatted(account.get("project_id").asText());
        } else {
            this.accessToken = null;
            this.sendUrl = null;
        }
    }

    @Override
    public boolean available() {
        return accessToken != null;
    }

    @Override
    public PushResult send(DeviceToken device, PushContent content) {
        try {
            client.post().uri(sendUrl).contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(accessToken.get()))
                    .body(Map.of("message", message(device, content))).retrieve().toBodilessEntity();
            return PushResult.DELIVERED;
        } catch (RestClientResponseException e) {
            return classify(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            log.warn("Push delivery could not reach Firebase: {}", e.getMessage());
            return PushResult.RETRY;
        }
    }

    private Map<String, Object> message(DeviceToken device, PushContent content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("token", device.token());
        message.put("notification", Map.of("title", content.title(), "body", content.body()));
        message.put("data", content.data());
        message.put("android", Map.of("priority", "HIGH",
                "notification", Map.of("channel_id", settings.androidChannel())));
        message.put("apns", Map.of("headers", Map.of("apns-priority", "10"),
                "payload", Map.of("aps", Map.of("sound", "default"))));
        return message;
    }

    private PushResult classify(HttpStatusCode status, String body) {
        if (status.value() == 404 || body.contains("UNREGISTERED")) {
            return PushResult.INVALID_TOKEN;
        }
        log.warn("Firebase refused a push with status {}", status.value());
        return status.value() == 400 ? PushResult.INVALID_TOKEN : PushResult.RETRY;
    }
}

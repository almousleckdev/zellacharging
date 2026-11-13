package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class TwilioMessages {

    private static final Logger log = LoggerFactory.getLogger(TwilioMessages.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final TwilioSettings settings;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    TwilioMessages(TwilioSettings settings) {
        this.settings = settings;
    }

    void post(String recipient, Map<String, String> form) {
        try {
            HttpResponse<String> response = client.send(request(form), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.error("Twilio refused the message for {}: HTTP {} {}", recipient, response.statusCode(),
                        response.body());
                throw unavailable();
            }
        } catch (IOException e) {
            log.error("Twilio could not be reached for {}: {}", recipient, e.getMessage());
            throw unavailable();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable();
        }
    }

    private HttpRequest request(Map<String, String> form) {
        String credentials = settings.key() + ":" + settings.secret();
        return HttpRequest.newBuilder(URI.create(settings.baseUrl() + "/2010-04-01/Accounts/" + settings.account()
                        + "/Messages.json"))
                .timeout(TIMEOUT)
                .header("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encoded(form)))
                .build();
    }

    private static String encoded(Map<String, String> form) {
        return form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CODE_NOT_SENT",
                "The verification code could not be sent");
    }
}

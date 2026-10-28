package com.evcharge.customer.wallet.infrastructure.pvit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;

class PvitSecretProvider {

    private static final Logger log = LoggerFactory.getLogger(PvitSecretProvider.class);
    private static final int MAX_LOGGED_BODY = 200;
    private static final Duration RENEW_MARGIN = Duration.ofMinutes(5);

    private final RestClient client;
    private final PvitProperties properties;
    private final ObjectMapper mapper;
    private String secret;
    private Instant renewAt = Instant.MIN;

    PvitSecretProvider(RestClient client, PvitProperties properties, ObjectMapper mapper) {
        this.client = client;
        this.properties = properties;
        this.mapper = mapper;
    }

    synchronized String current() {
        if (secret == null || !Instant.now().isBefore(renewAt)) {
            renew();
        }
        return secret;
    }

    synchronized void invalidate() {
        secret = null;
    }

    private static String abbreviated(String body) {
        return body.length() <= MAX_LOGGED_BODY ? body : body.substring(0, MAX_LOGGED_BODY);
    }

    private void renew() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("operationAccountCode", properties.operationAccountCode());
        form.add("password", properties.secretPassword());
        try {
            String body = client.post().uri("/v2/{code}/renew-secret", properties.codeUrlSecret())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(String.class);
            JsonNode json = mapper.readTree(body);
            String renewed = json.path("secret").asString(null);
            if (renewed == null || renewed.isBlank()) {
                throw new PvitException("The provider returned no secret key", false);
            }
            secret = renewed;
            renewAt = Instant.now().plusSeconds(json.path("expires_in").asLong(3600)).minus(RENEW_MARGIN);
            log.info("Payment provider secret key renewed");
        } catch (RestClientResponseException e) {
            secret = null;
            log.warn("Payment provider refused the secret renewal: HTTP {} {}", e.getStatusCode().value(),
                    abbreviated(e.getResponseBodyAsString()));
            throw new PvitException("The provider secret key could not be renewed", e);
        } catch (RestClientException e) {
            secret = null;
            log.warn("Payment provider unreachable for the secret renewal: {}", e.getClass().getSimpleName());
            throw new PvitException("The provider secret key could not be renewed", e);
        }
    }
}

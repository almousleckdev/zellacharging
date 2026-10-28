package com.evcharge.customer.wallet.infrastructure.pvit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class PvitKyc {

    private static final Logger log = LoggerFactory.getLogger(PvitKyc.class);
    private static final int EXCERPT_LIMIT = 200;

    private final RestClient client;
    private final PvitSecretProvider secrets;
    private final PvitProperties properties;
    private final ObjectMapper mapper;

    PvitKyc(RestClient client, PvitSecretProvider secrets, PvitProperties properties, ObjectMapper mapper) {
        this.client = client;
        this.secrets = secrets;
        this.properties = properties;
        this.mapper = mapper;
    }

    void inspect(String operatorCode, String customerNumber) {
        if (properties.codeUrlKyc() == null || properties.codeUrlKyc().isBlank()) {
            return;
        }
        try {
            String body = client.get()
                    .uri("/v2/{code}/kyc?customerAccountNumber={number}&operatorCode={operator}",
                            properties.codeUrlKyc(), customerNumber, operatorCode)
                    .header("X-Secret", secrets.current()).retrieve().body(String.class);
            JsonNode data = mapper.readTree(body);
            log.info("PVit KYC for {} answered with fields {}", operatorCode, data.propertyNames());
        } catch (RestClientResponseException e) {
            log.warn("PVit KYC for {} answered {}: {}", operatorCode, e.getStatusCode().value(),
                    excerpt(e.getResponseBodyAsString()));
        } catch (RuntimeException e) {
            log.warn("PVit KYC for {} unavailable: {}", operatorCode, e.getClass().getSimpleName());
        }
    }

    private static String excerpt(String body) {
        return body.length() <= EXCERPT_LIMIT ? body : body.substring(0, EXCERPT_LIMIT);
    }
}

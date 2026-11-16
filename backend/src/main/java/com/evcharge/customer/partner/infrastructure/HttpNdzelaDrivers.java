package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.NdzelaDriver;
import com.evcharge.customer.partner.NdzelaDrivers;
import com.evcharge.customer.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
class HttpNdzelaDrivers implements NdzelaDrivers {

    private static final Logger log = LoggerFactory.getLogger(HttpNdzelaDrivers.class);

    private final NdzelaSettings settings;
    private final RestClient client;

    HttpNdzelaDrivers(NdzelaSettings settings) {
        this.settings = settings;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public Optional<NdzelaDriver> find(UUID customerId) {
        if (!settings.configured()) {
            throw unavailable();
        }
        try {
            return Optional.ofNullable(client.get()
                    .uri(settings.baseUrl() + "/api/v1/partners/zella/customers/{id}/driver", customerId)
                    .header("X-Api-Key", settings.apiKey())
                    .retrieve()
                    .body(NdzelaDriver.class));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            log.warn("NDZela refused the driver request with status {}", e.getStatusCode().value());
            throw unavailable();
        } catch (RestClientException e) {
            log.warn("NDZela could not be reached: {}", e.getMessage());
            throw unavailable();
        }
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "NDZELA_UNAVAILABLE", "NDZela is not reachable");
    }
}

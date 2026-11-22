package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.NdzelaDriver;
import com.evcharge.customer.partner.NdzelaDrivers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Component
class HttpNdzelaDrivers implements NdzelaDrivers {

    private static final Logger log = LoggerFactory.getLogger(HttpNdzelaDrivers.class);

    private final NdzelaRestClient ndzela;

    HttpNdzelaDrivers(NdzelaRestClient ndzela) {
        this.ndzela = ndzela;
    }

    @Override
    public Optional<NdzelaDriver> find(UUID customerId) {
        try {
            return Optional.ofNullable(ndzela.getForCustomer("/driver", customerId).body(NdzelaDriver.class));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            log.warn("NDZela refused the driver request with status {}", e.getStatusCode().value());
            throw NdzelaRestClient.unavailable();
        } catch (RestClientException e) {
            log.warn("NDZela could not be reached: {}", e.getMessage());
            throw NdzelaRestClient.unavailable();
        }
    }
}

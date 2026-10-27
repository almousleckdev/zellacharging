package com.evcharge.customer.shared.ocpp;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcppSecureChannelTest {

    private final OcppClientConfiguration configuration = new OcppClientConfiguration();

    @Test
    void aPlainHttpLinkIsRefusedWhenASecureChannelIsRequired() {
        OcppClientProperties properties = new OcppClientProperties("http://ocpp.example", "key", "secret", true, 55, 10);
        assertThatThrownBy(() -> configuration.ocppRestClient(properties, Clock.systemUTC(), new SimpleMeterRegistry()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aMissingSigningSecretIsRefusedWhenASecureChannelIsRequired() {
        OcppClientProperties properties = new OcppClientProperties("https://ocpp.example", "key", "", true, 55, 10);
        assertThatThrownBy(() -> configuration.ocppRestClient(properties, Clock.systemUTC(), new SimpleMeterRegistry()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void httpsWithSigningIsAccepted() {
        OcppClientProperties properties = new OcppClientProperties("https://ocpp.example", "key", "secret", true, 55, 10);
        assertThat(configuration.ocppRestClient(properties, Clock.systemUTC(), new SimpleMeterRegistry())).isNotNull();
    }

    @Test
    void localDevelopmentMayRunUnsecured() {
        OcppClientProperties properties = new OcppClientProperties("http://localhost:8080", "key", "", false, 55, 10);
        assertThat(configuration.ocppRestClient(properties, Clock.systemUTC(), new SimpleMeterRegistry())).isNotNull();
    }
}

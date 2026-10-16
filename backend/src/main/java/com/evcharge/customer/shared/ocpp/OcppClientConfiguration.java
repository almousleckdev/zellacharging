package com.evcharge.customer.shared.ocpp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OcppClientProperties.class)
public class OcppClientConfiguration {

    @Bean
    RestClient ocppRestClient(OcppClientProperties properties, Clock clock) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-API-Key", properties.apiKey())
                .requestFactory(factory);
        if (properties.signingEnabled()) {
            builder.requestInterceptor(new SigningInterceptor(new RequestSigner(properties.signingSecret()), clock));
        }
        return builder.build();
    }
}

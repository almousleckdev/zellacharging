package com.evcharge.customer.wallet.infrastructure.pvit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(PvitProperties.class)
@ConditionalOnProperty(name = "zella.payment.provider", havingValue = "pvit")
class PvitConfiguration {

    @Bean
    PvitGateway pvitGateway(PvitProperties properties, ObjectMapper mapper) {
        requireComplete(properties);
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.requestTimeout()).build());
        factory.setReadTimeout(properties.requestTimeout());
        RestClient client = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
        PvitSecretProvider secrets = new PvitSecretProvider(client, properties, mapper);
        return new PvitGateway(new PvitApi(client, secrets, properties, mapper));
    }

    private static void requireComplete(PvitProperties properties) {
        Map<String, String> required = new LinkedHashMap<>();
        required.put("PVIT_BASE_URL", properties.baseUrl());
        required.put("PVIT_OPERATION_ACCOUNT_CODE", properties.operationAccountCode());
        required.put("PVIT_SECRET_PASSWORD", properties.secretPassword());
        required.put("PVIT_CODE_URL_SECRET", properties.codeUrlSecret());
        required.put("PVIT_CODE_URL_PAYMENT", properties.codeUrlPayment());
        required.put("PVIT_CODE_URL_STATUS", properties.codeUrlStatus());
        required.put("PVIT_CALLBACK_URL_CODE", properties.callbackUrlCode());
        required.put("PVIT_AGENT", properties.agent());
        required.put("PVIT_PRODUCT", properties.product());
        String missing = required.entrySet().stream().filter(entry -> entry.getValue() == null
                || entry.getValue().isBlank()).map(Map.Entry::getKey).reduce((a, b) -> a + ", " + b).orElse("");
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Payment provider pvit is selected but these settings are missing: " + missing);
        }
    }
}

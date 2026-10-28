package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.domain.MobileOperator;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

class PvitWiring {

    private final PvitProperties properties;
    private final ObjectMapper mapper;
    private final RestClient client;
    private final Map<MobileOperator, PvitSecretProvider> secrets = new EnumMap<>(MobileOperator.class);

    PvitWiring(PvitProperties properties, ObjectMapper mapper) {
        requireComplete(properties);
        this.properties = properties;
        this.mapper = mapper;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.requestTimeout()).build());
        factory.setReadTimeout(properties.requestTimeout());
        this.client = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
    }

    PvitGateway collection(MobileOperator operator) {
        return new PvitGateway(operator, api(operator), new PvitKyc(client, secretsOf(operator), properties, mapper));
    }

    PvitPayoutGateway payout(MobileOperator operator) {
        return new PvitPayoutGateway(operator, api(operator));
    }

    private PvitApi api(MobileOperator operator) {
        return new PvitApi(client, secretsOf(operator), properties, properties.accountOf(operator).orElseThrow(),
                mapper);
    }

    private PvitSecretProvider secretsOf(MobileOperator operator) {
        return secrets.computeIfAbsent(operator, key -> new PvitSecretProvider(client, properties,
                properties.accountOf(key).orElseThrow(), mapper));
    }

    private static void requireComplete(PvitProperties properties) {
        Map<String, String> required = new LinkedHashMap<>();
        required.put("PVIT_BASE_URL", properties.baseUrl());
        required.put("PVIT_SECRET_PASSWORD", properties.secretPassword());
        required.put("PVIT_CODE_URL_SECRET", properties.codeUrlSecret());
        required.put("PVIT_CODE_URL_PAYMENT", properties.codeUrlPayment());
        required.put("PVIT_CODE_URL_STATUS", properties.codeUrlStatus());
        required.put("PVIT_CALLBACK_URL_CODE", properties.callbackUrlCode());
        required.put("PVIT_AGENT", properties.agent());
        required.put("PVIT_PRODUCT", properties.product());
        String missing = required.entrySet().stream().filter(entry -> entry.getValue() == null
                || entry.getValue().isBlank()).map(Map.Entry::getKey).reduce((a, b) -> a + ", " + b).orElse("");
        boolean noAccount = properties.accountOf(MobileOperator.AIRTEL).isEmpty()
                && properties.accountOf(MobileOperator.MOOV).isEmpty();
        if (noAccount) {
            missing = (missing.isEmpty() ? "" : missing + ", ")
                    + "PVIT_COMPTE_OPERATION_AIRTEL or PVIT_COMPTE_OPERATION_MOOV";
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Payment provider pvit is selected but these settings are missing: "
                    + missing);
        }
    }
}

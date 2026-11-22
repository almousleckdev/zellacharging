package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.shared.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
class NdzelaRestClient {

    private static final String MANAGEMENT = "/api/v1/partners/zella/free-charging";

    private final NdzelaSettings settings;
    private final RestClient client;

    @Autowired
    NdzelaRestClient(NdzelaSettings settings) {
        this(settings, Duration.ofSeconds(3), Duration.ofSeconds(5));
    }

    NdzelaRestClient(NdzelaSettings settings, Duration connectTimeout, Duration readTimeout) {
        this.settings = settings;
        HttpClient http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(readTimeout);
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    RestClient.ResponseSpec getForCustomer(String pathSuffix, UUID customerId) {
        requireConfigured(NdzelaRestClient::unavailable);
        return guarded(client.get()
                .uri(settings.baseUrl() + "/api/v1/partners/zella/customers/{id}" + pathSuffix, customerId)
                .header("X-Api-Key", settings.apiKey())
                .retrieve(), NdzelaRestClient::unavailable);
    }

    RestClient.ResponseSpec manage(HttpMethod method, String path, String actor, Object body) {
        requireConfigured(NdzelaFailure::unavailable);
        RestClient.RequestBodySpec request = client.method(method)
                .uri(settings.baseUrl() + MANAGEMENT + path)
                .header("X-Api-Key", settings.apiKey())
                .header("X-Actor", actor);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return guarded(request.retrieve(), NdzelaFailure::unavailable);
    }

    private void requireConfigured(Supplier<ApiException> refusal) {
        if (!settings.configured()) {
            throw refusal.get();
        }
    }

    private static RestClient.ResponseSpec guarded(RestClient.ResponseSpec response,
                                                   Supplier<ApiException> refusal) {
        return response.onStatus(HttpStatusCode::is3xxRedirection, (request, answer) -> {
            throw refusal.get();
        });
    }

    static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "NDZELA_UNAVAILABLE", "NDZela is not reachable");
    }
}

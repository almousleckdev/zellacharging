package com.evcharge.customer.shared.ocpp;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;

class BudgetInterceptor implements ClientHttpRequestInterceptor {

    private static final Duration DEFAULT_BACKOFF = Duration.ofSeconds(30);
    private static final int TOO_MANY_REQUESTS = 429;
    private static final String BACKGROUND_PREFIX = "/api/v1/sessions";

    private final OcppRequestBudget budget;

    BudgetInterceptor(OcppRequestBudget budget) {
        this.budget = budget;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (HttpMethod.POST.equals(request.getMethod())) {
            budget.recordPriority();
        } else if (!budget.allow(isBackground(request))) {
            throw new OcppThrottledException();
        }
        ClientHttpResponse response = execution.execute(request, body);
        if (response.getStatusCode().value() == TOO_MANY_REQUESTS) {
            budget.backOff(retryAfter(response));
        }
        return response;
    }

    private static boolean isBackground(HttpRequest request) {
        return request.getURI().getPath().startsWith(BACKGROUND_PREFIX);
    }

    private static Duration retryAfter(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst("Retry-After");
        try {
            return header == null ? DEFAULT_BACKOFF : Duration.ofSeconds(Long.parseLong(header.trim()));
        } catch (NumberFormatException e) {
            return DEFAULT_BACKOFF;
        }
    }
}

package com.evcharge.customer.shared.ocpp;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

class SigningInterceptor implements ClientHttpRequestInterceptor {

    private final RequestSigner signer;
    private final Clock clock;

    SigningInterceptor(RequestSigner signer, Clock clock) {
        this.signer = signer;
        this.clock = clock;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String timestamp = Long.toString(clock.instant().getEpochSecond());
        String nonce = UUID.randomUUID().toString();
        String signature = signer.sign(request.getMethod().name(), request.getURI().getRawPath(),
                request.getURI().getRawQuery(), timestamp, nonce, body);
        request.getHeaders().set("X-Signature", signature);
        request.getHeaders().set("X-Signature-Timestamp", timestamp);
        request.getHeaders().set("X-Signature-Nonce", nonce);
        return execution.execute(request, body);
    }
}

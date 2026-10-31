package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.ProfilePictureFetcher;
import com.evcharge.customer.account.domain.Avatar;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
class HttpProfilePictureFetcher implements ProfilePictureFetcher {

    private static final String TRUSTED_HOST_SUFFIX = ".googleusercontent.com";
    private static final int MAX_BYTES = 1_000_000;
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public Optional<Avatar> fetch(String url) {
        Optional<URI> uri = trusted(url);
        if (uri.isEmpty()) {
            return Optional.empty();
        }
        try {
            HttpResponse<InputStream> response = client.send(
                    HttpRequest.newBuilder(uri.get()).timeout(TIMEOUT).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            return read(response);
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    static Optional<URI> trusted(String url) {
        try {
            URI uri = URI.create(url);
            boolean allowed = "https".equals(uri.getScheme()) && uri.getHost() != null
                    && uri.getHost().endsWith(TRUSTED_HOST_SUFFIX) && uri.getUserInfo() == null;
            return allowed ? Optional.of(uri) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Optional<Avatar> read(HttpResponse<InputStream> response) throws IOException {
        if (response.statusCode() != 200) {
            response.body().close();
            return Optional.empty();
        }
        String type = response.headers().firstValue("Content-Type").orElse("");
        try (InputStream body = response.body()) {
            byte[] data = body.readNBytes(MAX_BYTES + 1);
            return data.length == 0 || data.length > MAX_BYTES ? Optional.empty()
                    : Optional.of(new Avatar(type, data));
        }
    }
}

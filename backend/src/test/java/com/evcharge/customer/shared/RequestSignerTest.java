package com.evcharge.customer.shared;

import com.evcharge.customer.shared.ocpp.RequestSigner;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RequestSignerTest {

    private final RequestSigner signer = new RequestSigner("secret-de-test");

    @Test
    void matchesTheReferenceVectorPublishedByThePlatform() {
        String signature = signer.sign("POST", "/api/v1/charge-points/CP-1/connectors/1/start", "",
                "1757000000", "nonce-fixe", "{}".getBytes(StandardCharsets.UTF_8));
        assertThat(signature).isEqualTo("d2cab0ac251b7d7cf3cc78899a4a984d3cbf256d3a4cd6293dbd28015b5a625f");
    }

    @Test
    void queryAndBodyChangeTheSignature() {
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        String base = signer.sign("GET", "/api/v1/sessions", "idTag=A", "1757000000", "n-1234567", body);
        assertThat(signer.sign("GET", "/api/v1/sessions", "idTag=B", "1757000000", "n-1234567", body))
                .isNotEqualTo(base);
        assertThat(signer.sign("GET", "/api/v1/sessions", "idTag=A", "1757000000", "n-1234567", new byte[0]))
                .isNotEqualTo(base);
    }
}

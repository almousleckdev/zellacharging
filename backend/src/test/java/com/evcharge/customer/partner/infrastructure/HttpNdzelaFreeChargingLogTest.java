package com.evcharge.customer.partner.infrastructure;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class HttpNdzelaFreeChargingLogTest {

    private final UUID customer = UUID.randomUUID();

    private static HttpNdzelaFreeCharging client(int port) {
        return new HttpNdzelaFreeCharging(new NdzelaRestClient(new NdzelaSettings("http://127.0.0.1:" + port, "k"),
                Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @Test
    void anUnreachableNdzelaIsLoggedWithoutTheCustomerId(CapturedOutput output) throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        client(port).check(customer);

        assertThat(output.getOut() + output.getErr()).contains("NDZela free-charging check failed")
                .doesNotContain(customer.toString());
    }

    @Test
    void anErrorBodyNamingTheCustomerIsNeverLogged(CapturedOutput output) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] bytes = ("{\"detail\":\"customer " + customer + " unknown\"}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            client(server.getAddress().getPort()).check(customer);
        } finally {
            server.stop(0);
        }

        assertThat(output.getOut() + output.getErr()).contains("status 500").doesNotContain(customer.toString());
    }
}

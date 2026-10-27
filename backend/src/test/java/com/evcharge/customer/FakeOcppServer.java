package com.evcharge.customer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FakeOcppServer {

    public record Reply(int status, String body) {
    }

    private final Map<String, Reply> routes = new ConcurrentHashMap<>();
    private final Map<String, java.util.concurrent.atomic.AtomicInteger> hits = new ConcurrentHashMap<>();
    private final HttpServer server;

    public FakeOcppServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", this::handle);
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public FakeOcppServer route(String method, String path, int status, String body) {
        routes.put(method + " " + path, new Reply(status, body));
        return this;
    }

    public FakeOcppServer station(String id) {
        return station(id, "GA", true, "Available");
    }

    public FakeOcppServer station(String id, String country, boolean online, String connectorStatus) {
        return route("GET", "/api/v1/charge-points/" + id, 200, "{\"id\":\"" + id + "\",\"isoCode\":\"" + country
                + "\",\"online\":" + online + ",\"connectors\":[{\"connectorId\":1,\"status\":\""
                + connectorStatus + "\",\"connectorType\":\"CCS2\",\"maxPowerKw\":60}]}");
    }

    public int hits(String method, String path) {
        var counter = hits.get(method + " " + path);
        return counter == null ? 0 : counter.get();
    }

    public void resetHits() {
        hits.clear();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        exchange.getRequestBody().readAllBytes();
        hits.computeIfAbsent(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath(),
                key -> new java.util.concurrent.atomic.AtomicInteger()).incrementAndGet();
        Reply reply = routes.getOrDefault(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath(),
                new Reply(404, ""));
        byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(reply.status(), bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            exchange.getResponseBody().write(bytes);
        }
        exchange.close();
    }
}

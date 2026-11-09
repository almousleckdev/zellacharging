package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.IdTagRepository;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdTagService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int ATTEMPTS = 3;

    private final SecureRandom random = new SecureRandom();
    private final IdTagRepository idTags;
    private final ChargingGateway gateway;

    public IdTagService(IdTagRepository idTags, ChargingGateway gateway) {
        this.idTags = idTags;
        this.gateway = gateway;
    }

    public String ensure(UUID customerId) {
        return idTags.find(customerId).orElseGet(() -> allocate(customerId));
    }

    public String replace(UUID customerId) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            Optional<String> candidate = registered();
            if (candidate.isPresent()) {
                idTags.replace(customerId, candidate.get());
                return candidate.get();
            }
        }
        throw unavailable();
    }

    private String allocate(UUID customerId) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            Optional<String> candidate = registered();
            if (candidate.isPresent() && idTags.saveIfAbsent(customerId, candidate.get())) {
                return candidate.get();
            }
            var existing = idTags.find(customerId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        throw unavailable();
    }

    private Optional<String> registered() {
        String candidate = generate();
        return gateway.registerIdTag(candidate) ? Optional.of(candidate) : Optional.empty();
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ID_TAG_UNAVAILABLE",
                "Could not prepare the charging identity");
    }

    private String generate() {
        StringBuilder tag = new StringBuilder("Z-");
        for (int i = 0; i < 12; i++) {
            tag.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return tag.toString();
    }
}

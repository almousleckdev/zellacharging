package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.AvatarRepository;
import com.evcharge.customer.account.domain.Avatar;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarService {

    static final int MAX_BYTES = 1_000_000;
    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final AvatarRepository avatars;
    private final Clock clock;

    public AvatarService(AvatarRepository avatars, Clock clock) {
        this.avatars = avatars;
        this.clock = clock;
    }

    public void save(UUID customerId, String contentType, byte[] data) {
        String type = essence(contentType);
        if (!TYPES.contains(type) || data == null || data.length == 0 || data.length > MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AVATAR", "The picture is not accepted");
        }
        avatars.save(customerId, new Avatar(type, data), clock.instant());
    }

    private static String essence(String contentType) {
        if (contentType == null) {
            return "";
        }
        int parameters = contentType.indexOf(';');
        return (parameters < 0 ? contentType : contentType.substring(0, parameters)).trim().toLowerCase();
    }

    public Optional<Avatar> find(UUID customerId) {
        return avatars.find(customerId);
    }

    public void delete(UUID customerId) {
        avatars.delete(customerId);
    }
}

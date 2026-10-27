package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.domain.AdminUser;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface AdminTokenIssuer {

    String issueAccess(AdminUser admin);

    Duration accessTimeToLive();

    String issueChallenge(UUID adminId);

    Optional<UUID> readChallenge(String token);
}

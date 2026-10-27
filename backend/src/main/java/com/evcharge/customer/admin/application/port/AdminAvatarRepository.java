package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.domain.AdminAvatar;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AdminAvatarRepository {
    void save(UUID adminId, AdminAvatar avatar, Instant at);
    Optional<AdminAvatar> find(UUID adminId);
    Optional<Instant> updatedAt(UUID adminId);
    void delete(UUID adminId);
}

package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.AdminAvatarRepository;
import com.evcharge.customer.admin.domain.AdminAvatar;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcAdminAvatarRepository implements AdminAvatarRepository {

    private final JdbcClient jdbc;

    JdbcAdminAvatarRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(UUID adminId, AdminAvatar avatar, Instant at) {
        jdbc.sql("insert into admin_avatar (admin_id, content_type, data, updated_at) values (:id, :type, :data, :at) "
                        + "on conflict (admin_id) do update set content_type = :type, data = :data, updated_at = :at")
                .param("id", adminId).param("type", avatar.contentType()).param("data", avatar.data())
                .param("at", Timestamps.toDb(at)).update();
    }

    @Override
    public Optional<AdminAvatar> find(UUID adminId) {
        return jdbc.sql("select content_type, data from admin_avatar where admin_id = :id").param("id", adminId)
                .query((rs, rowNum) -> new AdminAvatar(rs.getString("content_type"), rs.getBytes("data"))).optional();
    }

    @Override
    public Optional<Instant> updatedAt(UUID adminId) {
        return jdbc.sql("select updated_at from admin_avatar where admin_id = :id").param("id", adminId)
                .query((rs, rowNum) -> Timestamps.read(rs, "updated_at")).optional();
    }

    @Override
    public void delete(UUID adminId) {
        jdbc.sql("delete from admin_avatar where admin_id = :id").param("id", adminId).update();
    }
}

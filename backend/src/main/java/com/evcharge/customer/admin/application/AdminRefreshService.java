package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminRefreshToken;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Sha256;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AdminRefreshService {

    private final AdminRefreshTokenRepository refreshTokens;
    private final AdminRepository admins;
    private final AdminSessionIssuer sessions;
    private final Clock clock;

    public AdminRefreshService(AdminRefreshTokenRepository refreshTokens, AdminRepository admins,
                               AdminSessionIssuer sessions, Clock clock) {
        this.refreshTokens = refreshTokens;
        this.admins = admins;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AdminTokens refresh(String rawToken) {
        Instant now = clock.instant();
        AdminRefreshToken token = refreshTokens.findByHash(Sha256.hex(rawToken))
                .orElseThrow(AdminRefreshService::invalid);
        if (token.isConsumed()) {
            refreshTokens.revokeFamily(token.familyId(), now);
            throw invalid();
        }
        if (token.isExpiredAt(now) || !refreshTokens.markUsed(token.id(), now)) {
            throw invalid();
        }
        AdminUser admin = admins.findById(token.adminId()).filter(AdminUser::active)
                .orElseThrow(AdminRefreshService::invalid);
        return sessions.issue(admin, token.familyId());
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByHash(Sha256.hex(rawToken))
                .ifPresent(token -> refreshTokens.revokeFamily(token.familyId(), clock.instant()));
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Invalid refresh token");
    }
}

package com.evcharge.customer.admin.web;

import com.evcharge.customer.account.Complimentary;
import com.evcharge.customer.admin.application.CustomerDetail;
import com.evcharge.customer.charging.CustomerSessions;
import com.evcharge.customer.wallet.CustomerWallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record CustomerDetailResponse(UUID id, String country, String firstName, String lastName, String email,
                              String phone, String status, boolean emailVerified, boolean phoneVerified,
                              Instant createdAt, Long avatarVersion, Wallet wallet, Sessions sessions,
                              FreeAccess freeAccess) {

    static CustomerDetailResponse from(CustomerDetail detail) {
        var customer = detail.customer();
        return new CustomerDetailResponse(customer.id(), detail.country(), customer.firstName(), customer.lastName(),
                customer.email(), customer.phone(), customer.status(), customer.emailVerified(),
                customer.phoneVerified(), customer.createdAt(), customer.avatarVersion(), Wallet.from(detail.wallet()),
                Sessions.from(detail.sessions()), FreeAccess.from(detail.complimentary()));
    }

    record FreeAccess(String reason, UUID grantedBy, Instant grantedAt, Instant expiresAt) {

        static FreeAccess from(Complimentary grant) {
            return grant == null ? null : new FreeAccess(grant.reason(), grant.grantedBy(), grant.grantedAt(),
                    grant.expiresAt());
        }
    }

    record Wallet(String currency, int decimals, long balanceMinor, long reservedMinor, long topupsSucceededMinor,
                  long pendingTopups, long withdrawalsPaidMinor) {

        static Wallet from(CustomerWallet wallet) {
            return wallet == null ? null : new Wallet(wallet.currency(), wallet.decimals(), wallet.balanceMinor(),
                    wallet.reservedMinor(), wallet.topupsSucceededMinor(), wallet.pendingTopups(),
                    wallet.withdrawalsPaidMinor());
        }
    }

    record Sessions(long completed, long live, BigDecimal energyKwh, long spentMinor, Instant lastSessionAt) {

        static Sessions from(CustomerSessions sessions) {
            return new Sessions(sessions.completed(), sessions.live(), sessions.energyKwh(), sessions.spentMinor(),
                    sessions.lastSessionAt());
        }
    }
}

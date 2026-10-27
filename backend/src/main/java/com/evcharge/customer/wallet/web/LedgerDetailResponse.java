package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.LedgerDetail;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.Withdrawal;

import java.time.Instant;
import java.util.UUID;

record LedgerDetailResponse(
        UUID id,
        String type,
        long amountMinor,
        long balanceAfterMinor,
        Instant createdAt,
        UUID sessionId,
        String operator,
        String msisdn,
        String state,
        String reference,
        String providerReference) {

    static LedgerDetailResponse from(LedgerDetail detail) {
        LedgerEntry entry = detail.entry();
        Movement movement = detail.topup().map(Movement::of)
                .or(() -> detail.withdrawal().map(Movement::of)).orElse(Movement.NONE);
        return new LedgerDetailResponse(entry.id(), entry.type().name(), entry.amountMinor(),
                detail.balanceAfterMinor(), entry.createdAt(), entry.sessionId(), movement.operator(),
                movement.msisdn(), movement.state(), movement.reference(), movement.providerReference());
    }

    private record Movement(String operator, String msisdn, String state, String reference,
                            String providerReference) {

        static final Movement NONE = new Movement(null, null, null, null, null);

        static Movement of(Topup topup) {
            return new Movement(topup.operator().name(), topup.msisdn(), topup.state().name(), topup.reference(),
                    topup.providerRef());
        }

        static Movement of(Withdrawal withdrawal) {
            return new Movement(withdrawal.operator().name(), withdrawal.msisdn(), withdrawal.state().name(),
                    withdrawal.reference(), withdrawal.providerRef());
        }
    }
}

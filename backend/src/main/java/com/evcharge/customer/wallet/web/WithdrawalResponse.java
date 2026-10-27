package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.Withdrawal;

import java.time.Instant;
import java.util.UUID;

record WithdrawalResponse(UUID id, String state, long amountMinor, String operator, String failureCode,
                          Instant createdAt) {

    static WithdrawalResponse from(Withdrawal withdrawal) {
        return new WithdrawalResponse(withdrawal.id(), withdrawal.state().name(), withdrawal.amountMinor(),
                withdrawal.operator().name(), withdrawal.failureCode(), withdrawal.createdAt());
    }
}

package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.RegisteredAccount;
import com.evcharge.customer.account.domain.Channel;

import java.util.UUID;

record RegisterResponse(UUID customerId, Channel channel, String target) {

    static RegisterResponse from(RegisteredAccount account) {
        return new RegisterResponse(account.customerId(), account.toVerify().channel(), account.toVerify().target());
    }
}

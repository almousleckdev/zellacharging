package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.Channel;

public interface VerificationSender {
    void send(Channel channel, String target, String code);
}

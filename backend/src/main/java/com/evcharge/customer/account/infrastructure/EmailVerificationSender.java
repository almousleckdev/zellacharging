package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.Channel;
import org.springframework.stereotype.Component;

@Component
class EmailVerificationSender implements VerificationSender {

    private final EmailCodeSender email;

    EmailVerificationSender(EmailCodeSender email) {
        this.email = email;
    }

    @Override
    public void send(Channel channel, String target, String code) {
        if (channel != Channel.EMAIL) {
            throw new IllegalArgumentException("Verification codes are only sent by email");
        }
        email.send(target, code);
    }
}

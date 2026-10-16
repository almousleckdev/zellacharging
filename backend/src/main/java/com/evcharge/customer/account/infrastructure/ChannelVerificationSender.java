package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.Channel;
import org.springframework.stereotype.Component;

@Component
class ChannelVerificationSender implements VerificationSender {

    private final EmailCodeSender email;
    private final LogPhoneCodeSender phone;

    ChannelVerificationSender(EmailCodeSender email, LogPhoneCodeSender phone) {
        this.email = email;
        this.phone = phone;
    }

    @Override
    public void send(Channel channel, String target, String code) {
        if (channel == Channel.EMAIL) {
            email.send(target, code);
            return;
        }
        phone.send(target, code);
    }
}

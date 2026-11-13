package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.CodeDelivery;
import org.springframework.stereotype.Component;

@Component
class ChannelVerificationSender implements VerificationSender {

    private final EmailCodeSender email;
    private final WhatsAppCodeSender whatsApp;
    private final SmsCodeSender sms;

    ChannelVerificationSender(EmailCodeSender email, WhatsAppCodeSender whatsApp, SmsCodeSender sms) {
        this.email = email;
        this.whatsApp = whatsApp;
        this.sms = sms;
    }

    @Override
    public void send(CodeDelivery delivery, String target, String code) {
        switch (delivery) {
            case EMAIL -> email.send(target, code);
            case WHATSAPP -> whatsApp.send(target, code);
            case SMS -> sms.send(target, code);
        }
    }
}

package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.CodeDelivery;

public interface VerificationSender {
    void send(CodeDelivery delivery, String target, String code);
}

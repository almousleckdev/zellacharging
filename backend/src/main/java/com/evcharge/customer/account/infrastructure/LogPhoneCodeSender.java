package com.evcharge.customer.account.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LogPhoneCodeSender {

    private static final Logger log = LoggerFactory.getLogger(LogPhoneCodeSender.class);

    void send(String phone, String code) {
        log.info("OTP for {}: {}", phone, code);
    }
}

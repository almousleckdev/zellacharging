package com.evcharge.customer.account.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ndzela.support")
public record SupportSettings(String email, String phone, String whatsapp, String agreementUrl) {
}

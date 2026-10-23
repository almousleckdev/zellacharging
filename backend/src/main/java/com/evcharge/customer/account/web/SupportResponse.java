package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.SupportSettings;

record SupportResponse(String email, String phone, String whatsapp, String agreementUrl) {

    static SupportResponse from(SupportSettings settings) {
        return new SupportResponse(blankToNull(settings.email()), blankToNull(settings.phone()),
                blankToNull(settings.whatsapp()), blankToNull(settings.agreementUrl()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

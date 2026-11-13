package com.evcharge.customer.account.domain;

public enum CodeDelivery {
    EMAIL,
    WHATSAPP,
    SMS;

    public boolean isPhone() {
        return this != EMAIL;
    }

    public static CodeDelivery defaultFor(Channel channel) {
        return channel == Channel.PHONE ? WHATSAPP : EMAIL;
    }
}

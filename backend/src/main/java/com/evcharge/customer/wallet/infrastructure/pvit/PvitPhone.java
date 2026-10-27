package com.evcharge.customer.wallet.infrastructure.pvit;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

final class PvitPhone {

    private PvitPhone() {
    }

    static String nationalFormat(String e164) {
        try {
            long national = PhoneNumberUtil.getInstance().parse(e164, null).getNationalNumber();
            return "0" + national;
        } catch (NumberParseException e) {
            throw new PvitException("The mobile money number cannot be read", true);
        }
    }
}

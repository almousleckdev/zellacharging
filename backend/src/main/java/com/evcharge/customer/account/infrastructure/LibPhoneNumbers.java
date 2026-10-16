package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.PhoneNumbers;
import com.evcharge.customer.shared.ApiException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class LibPhoneNumbers implements PhoneNumbers {

    private static final String DEFAULT_REGION = "GA";

    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

    @Override
    public String toE164(String raw) {
        try {
            Phonenumber.PhoneNumber parsed = util.parse(raw.trim(), DEFAULT_REGION);
            if (!util.isValidNumber(parsed)) {
                throw invalid();
            }
            return util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw invalid();
        }
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHONE", "Invalid phone number");
    }
}

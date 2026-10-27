package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.PhoneNumbers;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CountryDirectory;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class LibPhoneNumbers implements PhoneNumbers {

    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    private final CountryDirectory countries;

    LibPhoneNumbers(CountryDirectory countries) {
        this.countries = countries;
    }

    @Override
    public String toE164(String raw) {
        Phonenumber.PhoneNumber parsed = parse(raw.trim(), countries.defaultCountry().phoneRegion());
        if (!util.isValidNumber(parsed)) {
            throw invalid();
        }
        return util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    @Override
    public String regionOf(String e164) {
        String region = util.getRegionCodeForNumber(parse(e164, null));
        return region == null ? "" : region;
    }

    private Phonenumber.PhoneNumber parse(String raw, String defaultRegion) {
        try {
            return util.parse(raw, defaultRegion);
        } catch (NumberParseException e) {
            throw invalid();
        }
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHONE", "Invalid phone number");
    }
}

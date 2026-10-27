package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.AdminPhones;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
class LibPhoneAdminPhones implements AdminPhones {

    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

    @Override
    public Optional<String> normalize(String raw, Set<String> allowedRegions) {
        if (raw == null || !raw.trim().startsWith("+")) {
            return Optional.empty();
        }
        try {
            Phonenumber.PhoneNumber number = util.parse(raw.trim(), null);
            boolean accepted = util.isValidNumber(number) && allowedRegions.contains(util.getRegionCodeForNumber(number));
            return accepted ? Optional.of(util.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)) : Optional.empty();
        } catch (NumberParseException e) {
            return Optional.empty();
        }
    }
}

package com.evcharge.customer.partner.application;

import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
class DriverQueryValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@A-Z]+@[^\\s@A-Z]+\\.[^\\s@A-Z]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+[1-9][0-9]{7,14}$");
    private static final int MAX_REF = 100;

    private final PartnerSettings settings;

    DriverQueryValidator(PartnerSettings settings) {
        this.settings = settings;
    }

    void require(List<DriverQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_BATCH_EMPTY", "At least one driver is required");
        }
        if (queries.size() > settings.maxLookupBatch()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_BATCH_TOO_LARGE",
                    "At most " + settings.maxLookupBatch() + " drivers per request");
        }
        queries.forEach(DriverQueryValidator::requireValid);
    }

    private static void requireValid(DriverQuery query) {
        boolean valid = query != null && query.ref() != null && !query.ref().isBlank()
                && query.ref().length() <= MAX_REF && query.email() != null
                && EMAIL.matcher(query.email()).matches() && query.phoneE164() != null
                && PHONE.matcher(query.phoneE164()).matches();
        if (!valid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_CONTACT_INVALID",
                    "Each driver needs a ref, a lowercase email and an E.164 phone number");
        }
    }
}

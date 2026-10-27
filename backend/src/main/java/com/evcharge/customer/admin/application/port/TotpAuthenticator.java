package com.evcharge.customer.admin.application.port;

import java.time.Instant;
import java.util.OptionalLong;

public interface TotpAuthenticator {

    String newSecret();

    OptionalLong verify(String secret, String code, Instant now);

    String provisioningUri(String issuer, String account, String secret);
}

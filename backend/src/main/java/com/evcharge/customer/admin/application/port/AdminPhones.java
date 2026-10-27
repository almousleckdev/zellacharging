package com.evcharge.customer.admin.application.port;

import java.util.Optional;
import java.util.Set;

public interface AdminPhones {
    Optional<String> normalize(String raw, Set<String> allowedRegions);
}

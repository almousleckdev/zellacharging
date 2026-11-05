package com.evcharge.customer.charging;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CustomerIdTags {

    Map<UUID, String> existing(Collection<UUID> customerIds);
}

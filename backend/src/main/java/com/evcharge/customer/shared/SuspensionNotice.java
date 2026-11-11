package com.evcharge.customer.shared;

import java.util.OptionalLong;
import java.util.UUID;

public interface SuspensionNotice {

    OptionalLong owedMinor(UUID customerId);
}

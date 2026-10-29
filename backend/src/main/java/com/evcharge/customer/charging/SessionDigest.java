package com.evcharge.customer.charging;

import java.math.BigDecimal;

public record SessionDigest(String customerName, long sessions, BigDecimal energyKwh, long spentMinor,
                            SessionRecord last) {
}

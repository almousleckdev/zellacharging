package com.evcharge.customer.charging;

import java.util.List;

public record SessionDigestPage(long total, List<SessionDigest> items) {
}

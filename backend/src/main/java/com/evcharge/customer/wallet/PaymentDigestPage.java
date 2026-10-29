package com.evcharge.customer.wallet;

import java.util.List;

public record PaymentDigestPage(long total, List<PaymentDigest> items) {
}
